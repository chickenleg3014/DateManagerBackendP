"""
localdata.go.kr에서 받은 '문화_숙박업.csv'(지방행정 인허가 데이터)를 places 테이블에 적재하는 1회성 스크립트.

이 데이터셋은 KOPIS/TourAPI/박물관표준데이터처럼 실시간 API가 아니라 수동 다운로드한 CSV 스냅샷이라
Spring 예약(@Scheduled) 동기화 서비스로 만들지 않고 별도 스크립트로 분리했다.

사용법:
  1. localdata.go.kr에서 "문화_숙박업" 인허가 데이터를 CSV로 내려받는다 (CP949 인코딩, 용량이 커서 저장소에는 포함 안 함).
  2. 아래 환경변수를 설정한다 (.env와 동일한 키를 그대로 씀):
       ORACLE_USERNAME, ORACLE_PASSWORD, ORACLE_DSN(기본값 localhost:1521/XEPDB1), LODGING_CSV_PATH
  3. pip install oracledb pyproj
  4. python scripts/import_lodging.py

관리번호를 external_id로 써서 재실행해도 이미 들어간 행은 건너뛰고 신규 행만 추가한다(안전하게 재실행 가능).
좌표는 EPSG:5174(Bessel 중부원점 TM)로 내려오므로 pyproj로 WGS84(EPSG:4326)로 변환해서 저장한다.
"""
import csv
import os
from datetime import datetime

import oracledb
from pyproj import Transformer

CSV_PATH = os.environ.get("LODGING_CSV_PATH")
DB_USER = os.environ["ORACLE_USERNAME"]
DB_PASSWORD = os.environ["ORACLE_PASSWORD"]
DB_DSN = os.environ.get("ORACLE_DSN", "localhost:1521/XEPDB1")

EXTERNAL_SOURCE = "LODGING_STD"
CATEGORY = "숙박"

if not CSV_PATH:
    raise SystemExit("환경변수 LODGING_CSV_PATH에 문화_숙박업.csv 경로를 설정하세요.")

transformer = Transformer.from_crs("EPSG:5174", "EPSG:4326", always_xy=True)


def parse_coord(x_raw, y_raw):
    try:
        x = float(x_raw.strip())
        y = float(y_raw.strip())
        if x == 0 or y == 0:
            return None, None
        lon, lat = transformer.transform(x, y)
        return lat, lon
    except (ValueError, AttributeError):
        return None, None


def main():
    conn = oracledb.connect(user=DB_USER, password=DB_PASSWORD, dsn=DB_DSN)
    cur = conn.cursor()

    cur.execute("SELECT external_id FROM places WHERE external_source = :src", src=EXTERNAL_SOURCE)
    existing_ids = {row[0] for row in cur.fetchall()}
    print(f"기존에 이미 적재된 {EXTERNAL_SOURCE} 건수: {len(existing_ids)}")

    rows_to_insert = []
    seen_in_file = set()
    total = 0
    active = 0

    with open(CSV_PATH, "r", encoding="cp949", errors="replace") as f:
        reader = csv.DictReader(f)
        for row in reader:
            total += 1
            if row.get("영업상태명") != "영업/정상":
                continue
            active += 1

            mgt_no = (row.get("관리번호") or "").strip()
            if not mgt_no or mgt_no in existing_ids or mgt_no in seen_in_file:
                continue
            seen_in_file.add(mgt_no)

            name = (row.get("사업장명") or "").strip()
            if not name:
                continue

            address = (row.get("도로명주소") or "").strip() or (row.get("지번주소") or "").strip()
            lat, lon = parse_coord(row.get("좌표정보(X)", ""), row.get("좌표정보(Y)", ""))

            rows_to_insert.append({
                "name": name[:100],
                "category": CATEGORY,
                "address": address[:255] if address else None,
                "lat": lat,
                "lon": lon,
                "src": EXTERNAL_SOURCE,
                "eid": mgt_no[:50],
                "created": datetime.now(),
            })

    print(f"전체 행: {total}, 영업/정상: {active}, 신규 삽입 대상: {len(rows_to_insert)}")

    if rows_to_insert:
        insert_sql = """
            INSERT INTO places (name, category, address, latitude, longitude, external_source, external_id, created_at)
            VALUES (:name, :category, :address, :lat, :lon, :src, :eid, :created)
        """
        batch_size = 1000
        for i in range(0, len(rows_to_insert), batch_size):
            batch = rows_to_insert[i:i + batch_size]
            cur.executemany(insert_sql, batch)
            conn.commit()
            print(f"  {i + len(batch)}/{len(rows_to_insert)} 삽입 완료")

    cur.execute("SELECT COUNT(*) FROM places WHERE external_source = :src", src=EXTERNAL_SOURCE)
    print(f"최종 {EXTERNAL_SOURCE} 총 건수: {cur.fetchone()[0]}")

    cur.close()
    conn.close()


if __name__ == "__main__":
    main()

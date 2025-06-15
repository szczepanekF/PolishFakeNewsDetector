import demagog, fakenews
import psycopg2
from dotenv import load_dotenv
import os


def connect_db():
    host = 'localhost'
    dbname = os.getenv('EVALUATION_SOURCES_DB_NAME')
    user = os.getenv('POSTGRES_USER')
    password = os.getenv('POSTGRES_PASSWORD')
    try:
        conn = psycopg2.connect(
            host=host,
            dbname=dbname,
            user=user,
            password=password,
            port=5434
        )
        return conn
    except Exception as e:
        print(f"Error: {e}")
        return None

def get_last_scrapped_post_timestamp(conn):
    with conn.cursor() as cursor:
        query = """
        SELECT publication_date
        FROM claims
        ORDER BY publication_date DESC
        LIMIT 1;
        """
        cursor.execute(query)
        result = cursor.fetchone()
        if result:
            return result[0]
        else:
            return None

def save_to_db(conn, data):
    with conn.cursor() as cursor:
        insert_query = """
        INSERT INTO claims (content, source, label, publication_date, link)
        VALUES (%s, %s, %s, %s, %s)
        """
        cursor.executemany(insert_query, data)
        conn.commit()    


def scrap_data():
    load_dotenv(dotenv_path='../.env')
    conn = connect_db()
    timestamp = get_last_scrapped_post_timestamp(conn)
    data = demagog.scrap(timestamp)
    data += fakenews.scrap(timestamp)
    # print(len(data))
    save_to_db(conn, data)


# scrap_data()
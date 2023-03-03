#  This software shall not be used for commercial purposes, only for learning communication
#  Copyright (c) 2022-2023. All rights reserved.
import atexit
import random
import time
import psycopg2 as pg

from appium.webdriver.webdriver import WebDriver
from selenium.webdriver import Keys
from selenium.webdriver.common.by import By
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager

headers = {
    "authority": "www.webofscience.com",
    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
    "accept-language": "zh-CN,zh;q=0.9",
    "cache-control": "max-age=0",
    "referer": "https://access.clarivate.com/",
    "sec-ch-ua": "\"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"110\"",
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": "\"Windows\"",
    "sec-fetch-dest": "document",
    "sec-fetch-mode": "navigate",
    "sec-fetch-site": "same-origin",
    "sec-fetch-user": "?1",
    "upgrade-insecure-requests": "1",
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"
}
cookies = {
    "WOSSID": "USW2EC0BDDRXj0taQshzFHF2BDK5g",
    "dotmatics.elementalKey": "SLsLWlMhrHnTjDerSrlG",
    "bm_sz": "877B6C40088FB4ECE5FB3E857398D3A2~YAAQdYFtaA/wGTaGAQAAuXcIlhJHhJDPX3+pJHOT6I4qPCR1XdBMIu4P6q2ln3McNH7u0PrjWVnXwJ3zqkLapR/6xXupKwQZ4cJvcgDuIsRhhACcqh3KAXLf5NV1ubJdk3K1NceV180voVkkeHAldSeS73RKf/g9vQOxp3USHcQ7TM03TJD7rF5PMNqaKQgow252bRfXfLAPg9YiDPwTLTC/8Uq5k3qr0wM/NVBIO01JRN/od5iDNPp6Slr+D+5kFulfuvCHNThn+Zzx1fE5sS/8XmJD2fC5HfIZyIw5DtHDHh8+PnAvznw=~4277561~3551302",
    "ak_bmsc": "E7B65E9E3D77BA7481E33C4767C5FE31~000000000000000000000000000000~YAAQdYFtaGXwGTaGAQAAgn0IlhLigs7ez24YzqSqJUpSR6YN3XSrLWCafPYHSAt2aPlPQzqBUw/p7ZahlaidoXVbzcWt2ft0tdV+2dsZdvElI3lMvT2v3CiYbrZ59un6ZVQ57WFzktlzv+y4IHSG8drmAvHGVqT3zUwpC7BSudTzlR4WbWZ9veSXwUbvLmTFW7sPqw7KCS31R/MUnuwg0LSstv96FFHX/XwBe97g0r8KtTqMEQJCkkADlS0oriGw2bAxt4KxKu5XZTBSzQDMEma47FWDoURU/YsZGUWACluKLOrSkyUzE90PBbnyJaudfXTeJtMGXntqifMqUsOMuhC+l4SXLQUfTmRoevz59nwsbu+SKD0fnSL+u2qxu05YUseSup4ssxg7j1BbJ+eOaL2X13HfdqHA5Rimr2mPQJTeuqtP6QSm+5yAuyhNX1Gk54Jj3HDR/WOt7ztZWuBI214U8fBCFlcCKkqSrj+5XFgGlX/iobxcqEd5HpIUho4=",
    "_abck": "141C3680A8EF3DAB733F71420ABB7454~0~YAAQdYFtaJHwGTaGAQAAnH8IlgmkfV1WdreEJkZ6/itT8C6IIy3LMWbuUen7/h6S1Y2MfmGxBbj8JWMmLG0/uYDgQ7xi5dAeGxkrXTzIEYbFyW7UfjW/2Kr2CfzRFzSXohit9b8Y2V7VNxmTGoXUqJR50DqNrsrnLPHYAf8kmKdNi3dqEYot7M5xo2s2kdvyQ6S8BQKNM78sJUZxO+hZyd47UduKecPqyM4680EHhhaff09YZHzntYbtD4UcLZ2T0YMibnBIHluVPMcjQhWGzhHAQ2Nk50deT8oct9g1y9kYvoOzKs1vphthSCDVEtg0ArP+3Jm2P1XcE6hMNAcqzl7BVcxORa8KOheRUjqPnrRJ3ciN8qJ+P/M/CNrSJ/qJAFc8tLUrnWqFX4FAMMnZICr93+Vs/D4n++VhHicJ~-1~-1~-1",
    "_sp_ses.840c": "*",
    "_sp_id.840c": "f288de04-35a8-46ff-8f1f-c2fc793178a6.1677554386.1.1677554390..fedbc081-13ad-4d41-a17a-9188c0c618f6..f52fe789-65b8-4d1d-9cd3-14525d4c4429.1677554390443.1",
    "OptanonAlertBoxClosed": "2023-02-28T03:20:00.292Z",
    "OptanonConsent": "isGpcEnabled=0&datestamp=Tue+Feb+28+2023+11%3A20%3A00+GMT%2B0800+(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4)&version=6.39.0&isIABGlobal=false&hosts=&consentId=fdaef071-cd62-4153-9c5c-190c1cf9ccec&interactionCount=1&landingPath=NotLandingPage&groups=C0001%3A1%2CC0003%3A1%2CC0004%3A1%2CC0005%3A1%2CC0002%3A1",
    "bm_sv": "91DA4ACDF5FC6AB4B213C050CD844CAC~YAAQdYFtaIT0GTaGAQAAmsYIlhJyPVdaA0ZdTO2VMhZI+GpQZe8W27ngmneEOC7DOXkdlHTw87TkItOKHYq/pikPmYWS7t2CCfjOROqjVUVqZl+FG2O++UD3M0LPOvjOpbHMeArE3eQCgj4ZiqCYGKznminPfExjFHDHHq9CA9VPZmWUq1OW1idFJGOxKsugo9hCp472l/CxFHsKRlxC6YbARdo0jFpDGY/gUip7EmkkQLS749VQ891DgiFOgecXEmUJ/Lmf~1",
    "RT": "\"z=1&dm=www.webofscience.com&si=583820eb-09c0-43c1-b6c4-8c420c55a60d&ss=lenokder&sl=3&tt=5eu&bcn=%2F%2F684d0d48.akstat.io%2F&obo=1&ld=grl&r=1ugltftr&ul=grl\""
}
chrome_options = Options()
# chrome_options.add_argument('--headless')
chrome_options.add_argument('--disable-gpu')
chrome_options.add_argument('--no-sandbox')


def db_connect():
    return pg.connect(database="postgres", user="postgres", password="123456", host="182.160.14.59", port="30432")


def get_keyword():
    conn = db_connect()
    cursor = conn.cursor()
    sql = "select keyword from keyword where lock = 0 limit 1"
    cursor.execute(sql)
    keywords = cursor.fetchall()
    cursor.close()
    conn.close()
    return keywords[0]


keyword = get_keyword()


def search(driver: WebDriver, key_ward):
    retrieval = driver.find_element(
        By.XPATH, value='//input[@name="search-main-box"]')
    retrieval.send_keys(key_ward)
    confirm = driver.find_element(By.XPATH, value='//button[@type="submit"]')
    confirm.send_keys(Keys.ENTER)
    time.sleep(5)
    return driver


def scroll_(driver: WebDriver):
    js = "return action=document.body.scrollHeight"
    height = 0
    new_height = driver.execute_script(js)
    time.sleep(2)
    while height < new_height:
        for i in range(height, new_height, 300):
            driver.execute_script('window.scrollTo(0, {})'.format(i))
            time.sleep(0.3)
        height = new_height
        time.sleep(2)
        new_height = driver.execute_script(js)
    return driver


def acc_cookie(driver: WebDriver):
    try:
        cookie = driver.find_element(
            By.XPATH, value='//button[@id="onetrust-accept-btn-handler"]')
        if cookie:
            cookie.send_keys(Keys.ENTER)
    except Exception as e:
        pass
    return driver


def get_all_document(driver: WebDriver):
    driver = scroll_(acc_cookie(driver))
    documents = driver.find_elements(
        By.XPATH, value='//app-summary-title/h3/a')
    urls = []
    for document in documents:
        url = document.get_attribute("href")
        urls.append(url)
    return urls, driver


def get_all_info(driver: WebDriver, key_word, start_page: int = None, end_page: int = None):
    driver = search(driver, key_word)
    end_no = driver.find_elements(
        By.XPATH, value='//span[@class="end-page ng-star-inserted"]')[0].text
    start_page = start_page if start_page else 1
    end_page = end_page if end_page else int(end_no.replace(",", ""))

    if start_page > 1:
        next_page = driver.find_element(
            By.XPATH, value='//input[@id="snNextPageTop"]')
        next_page.clear()
        next_page.send_keys(str(start_page))
        next_page.send_keys(Keys.ENTER)

    try:
        while True:
            print(f'抓取关键词: {key_word}, 当前抓取第{start_page}页，共{end_no}页')
            save_page_db(start_page)
            page_url = driver.current_url
            print(f'当前页面地址: {page_url}')
            urls, driver = get_all_document(driver)
            print(f'抓取到论文地址: {urls}')
            print('===' * 40)
            for url in urls:
                get_info_by_url(driver, url)

            start_page += 1
            if end_page and start_page >= end_page:
                break
            next_page_url = page_url.replace(
                f'/relevance/{start_page - 1}', f'/relevance/{start_page}')
            while True:
                try:
                    driver.get(next_page_url)
                    break
                except Exception as e:
                    time.sleep(60)
    except Exception as e:
        print(e)
        print('异常 or 查询结束')


def get_info_by_url(driver: WebDriver, url):
    while True:
        try:
            driver.get(url)
            break
        except Exception as e:
            print(f'请求异常，休息1min重试: {url}')
            time.sleep(60)
    time.sleep(random.randint(5, 10))
    print('===' * 40)
    current_url = driver.current_url
    address_list = ''
    title = ''
    author = ''
    email_list = []
    try:
        title = driver.find_elements(
            By.XPATH, value='//h2[@id="FullRTa-fullRecordtitle-0"]')[0].text
        authors = driver.find_elements(
            By.XPATH, value='//span[starts-with(@id, "author-")]//span[@lang="en"]')
        authors = [author.text for author in authors]
        author = ','.join(authors)
        address_list = driver.find_elements(
            By.XPATH, value='//a[starts-with(@id, "address")]/span[2]')
        address_list = [address_en.text for address_en in address_list]
        for i in range(0, 20):
            try:
                email = \
                    driver.find_elements(By.XPATH, value=f'//a[@cdxanalyticscategory="wos-author-email-addresses"]')[
                        i].text
                email_list.append(email)
            except Exception as e:
                break
    except Exception as e:
        print(f'current_url: {current_url} 缺失部分信息')
    print(
        f'title: {title}  \nauthors: {author}  \nurl: {current_url}  \nemail: {email_list}')
    if email_list is not None:
        for email in email_list:
            save_to_db([title, address_list, author, email, current_url])


def save_to_db(data: list):
    conn = db_connect()
    cursor = conn.cursor()
    # 如果不存在相同email的记录，则插入
    sql = "select * from wos where email = %s"
    cursor.execute(sql, (data[3],))
    if cursor.fetchone():
        print(f'已存在相同email: {data[3]}')
        return
    sql = "insert into wos(title, address_list, author, email, url) values(%s, %s, %s, %s, %s)"
    cursor.execute(sql, data)
    conn.commit()
    cursor.close()
    conn.close()


def save_page_db(page: int):
    conn = db_connect()
    cursor = conn.cursor()
    sql = "update keyword set page = %s where keyword = %s"
    cursor.execute(sql, [page, keyword])
    conn.commit()
    cursor.close()
    conn.close()


def get_latest_page():
    conn = db_connect()
    cursor = conn.cursor()
    sql = "select page from keyword where keyword = %s limit 1"
    cursor.execute(sql, keyword)
    page = cursor.fetchone()
    cursor.close()
    conn.close()
    return page[0] if page else 1


def lock_keyword():
    conn = db_connect()
    cursor = conn.cursor()
    sql = "update keyword set lock = 1 where keyword = %s"
    cursor.execute(sql, keyword)
    conn.commit()
    cursor.close()
    conn.close()
    print(f'{keyword} 关键词已加锁')


def unlock_keyword():
    conn = db_connect()
    cursor = conn.cursor()
    sql = "update keyword set lock = 0 where keyword = %s"
    cursor.execute(sql, keyword)
    conn.commit()
    cursor.close()
    conn.close()
    print(f'{keyword} 关键词已解锁')


def run():
    lock_keyword()
    start = get_latest_page()
    driver = webdriver.Chrome(
        ChromeDriverManager().install(), chrome_options=chrome_options)
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": """
            Object.defineProperty(navigator, 'webdriver', {
              get: () => undefined
            })
          """
    })
    driver.get('https://www.baidu.com/')
    for name, value in cookies.items():
        driver.add_cookie({'name': name, 'value': value})
    start_page = 'https://www.webofscience.com/wos/alldb/basic-search'
    driver.get(start_page)
    acc_cookie(driver)
    get_all_info(driver, keyword, start, start + 10)
    driver.close()
    run()


atexit.register(unlock_keyword)

if __name__ == '__main__':
    run()

from selenium import webdriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.common.by import By
from selenium.webdriver.edge.options import Options
import time
import datetime

subsites = ['polityka', 'spoleczenstwo', 'technologia', 'zdrowie', 'srodowisko', 'badania']

months = {
    'stycznia': 1, 'lutego': 2, 'marca': 3,
    'kwietnia': 4, 'maja': 5, 'czerwca': 6,
    'lipca': 7, 'sierpnia': 8, 'września': 9,
    'października': 10, 'listopada': 11, 'grudnia': 12
}


def get_timestamp(date_str:str):
    day, month_name, year = date_str.split()
    month = months[month_name.lower()]
    dt = datetime.datetime(int(year), month, int(day))
    return dt

def collect_adrticles_data(articles:list[WebElement], claims:list, last_element_timestamp:float) ->  bool:
    for article in articles:
        claim = article.find_element(By.CLASS_NAME, 'post-title').find_element(By.TAG_NAME, 'h2').text
        try:
            label = article.find_element(By.CLASS_NAME, 'labelocena').text
        except Exception as e:
            label = "NOT DEFINED"
        # tags = subsite.upper()
        publication_timestamp = get_timestamp(article.find_element(By.CLASS_NAME, 'post-tags').find_elements(By.TAG_NAME, 'li')[0].text)
        # author = article.find_element(By.CLASS_NAME, 'post-tags').find_elements(By.TAG_NAME, 'li')[1].find_element(By.TAG_NAME, 'a').text
        link = article.find_element(By.CLASS_NAME, 'post-title').find_element(By.TAG_NAME, 'h2').find_element(By.TAG_NAME, 'a').get_attribute('href')
        if last_element_timestamp is None or (last_element_timestamp is not None and publication_timestamp > last_element_timestamp):
            claims.append((claim, 'fakenews_pl', label, publication_timestamp, link))
        else:
           return True
    return False

def scrap_pages(driver:webdriver.Edge, claims:list[tuple], last_scrapped_timestamp:float, subsite:str):
    page_number=1
    if last_scrapped_timestamp is None:
        stop_condition = lambda page_nubmber: page_number <= 3
    else:
        stop_condition = lambda page_number: True

    while stop_condition(page_number):
        next_button = driver.find_element(By.CLASS_NAME, 'next')
        articles = driver.find_elements(By.CLASS_NAME, 'news-post')
        finish = collect_adrticles_data(articles, claims, last_scrapped_timestamp)
        if finish:
            return
        driver.execute_script("arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", next_button)
        time.sleep(4)
        next_button.click()
        page_number += 1
        time.sleep(4)


def scrap_subsites(driver:webdriver.Edge, claims:list[tuple], last_scrapped_timestamp:float):
    source = "https://fakenews.pl/"
    for subsite in subsites:
        website_url = f"{source}/{subsite}/"
        driver.get(website_url)
        time.sleep(4)
        scrap_pages(driver, claims, last_scrapped_timestamp, subsite)

def scrap(last_scrapped_timestamp=None, headless=True) -> list[tuple]:
    claims = []
    edge_options = Options()
    if headless:
        edge_options.add_argument("--headless=new")
    edge_options.add_argument("--start-maximized")
    driver = webdriver.Edge(options=edge_options)
    try:
        scrap_subsites(driver, claims, last_scrapped_timestamp)
    finally:
        driver.quit()
        return sorted(list(set(claims)), key=lambda element: element[3])

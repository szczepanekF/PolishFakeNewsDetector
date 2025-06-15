from selenium.webdriver.common.by import By
import time
import datetime
import undetected_chromedriver as uc


def get_timestamp(date_str):
    dt = datetime.datetime.strptime(date_str, "%d.%m.%Y")
    return dt

def scrap_pages(driver:uc.Chrome, claims, last_scrapped_timestamp):
    page_number = 1
    if last_scrapped_timestamp is None:
        stop_condition = lambda page_nubmber: page_number <= 3
    else:
        stop_condition = lambda page_number: True

    while stop_condition(page_number):
        load_more_button = driver.find_element(By.CLASS_NAME, "dg-load-more")
        articles_container = driver.find_element(By.CLASS_NAME, "dg-listing-tiles__items")
        articles = articles_container.find_elements(By.CLASS_NAME, "column")
        finish = collect_adrticles_data(articles, claims, last_scrapped_timestamp)
        if finish:
            return
        driver.execute_script("arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", load_more_button)
        time.sleep(4) 
        load_more_button.click()
        page_number += 1
        time.sleep(4)
    

def collect_adrticles_data(articles:list[uc.WebElement], claims:list, last_element_timestamp:float) ->  bool:
    for article in articles:
        claim = article.find_element(By.CLASS_NAME, 'dg-item__description').text
        label = article.find_element(By.CLASS_NAME, 'dg-item__evaluation').find_element(By.TAG_NAME, 'p').text
        # tags = ",".join([tag.text for tag in article.find_element(By.CLASS_NAME, 'dg-item__tags').find_elements(By.TAG_NAME, 'a')])
        publication_timestamp = get_timestamp(article.find_element(By.CLASS_NAME, 'dg-item__header-info').find_element(By.TAG_NAME, 'span').text)
        # author = article.find_element(By.CLASS_NAME, 'dg-item__person').text
        link = article.find_element(By.CLASS_NAME, 'dg-item__title').find_element(By.TAG_NAME, 'a').get_attribute('href')
        if last_element_timestamp is None or (last_element_timestamp is not None and publication_timestamp > last_element_timestamp):
            claims.append((claim, 'demagog_org', label, publication_timestamp, link))
        else:
           return True
    return False

def scrap(last_scrapped_timestamp=None, headless=True) -> list[tuple]:
    source = "https://demagog.org.pl"
    website_url = f"{source}/wypowiedzi/"
    chrome_options = uc.ChromeOptions()
    if headless:
        chrome_options.add_argument("--headless=new")
        chrome_options.add_argument("--window-size=1920,1080")
        chrome_options.add_argument("--disable-blink-features=AutomationControlled")
        chrome_options.add_argument(
            "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/123.0.0.0 Safari/537.36"
        )
    else:
        chrome_options.add_argument("--start-maximized")

    driver = uc.Chrome(options=chrome_options)
    claims = []
    try:
        driver.get(website_url)
        time.sleep(4)
        scrap_pages(driver, claims, last_scrapped_timestamp)
    finally:
        driver.quit()
        return sorted(list(set(claims)), key=lambda element: element[3])


import requests
from bs4 import BeautifulSoup
response = requests.get("http://jecvay.com")
soup = BeautifulSoup(response.text)

print(soup.body.text.decode())

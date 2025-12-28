-- Ustawienie hasła dla głównego użytkownika (postgres)
 sudo -u postgres psql
 ALTER USER postgres PASSWORD 'twoje_haslo';
 
 -- Tworzenie bazy i użytkownika pod Twoją aplikację
 -- Tworzenie bazy danych
 CREATE DATABASE enterprise_shop_dev;
 
 -- Tworzenie użytkownika
 CREATE USER shop_dev WITH ENCRYPTED PASSWORD 'shop_dev';
 
 -- Nadanie uprawnień dla bazy użytkownikowi tej bazy
 GRANT ALL PRIVILEGES ON DATABASE enterprise_shop_dev TO shop_dev;
 
 
 
 -- Zalogowanie się jako superużytkownik i nadanie uprawnień
 psql -h localhost -U postgres -d enterprise_shop_dev
 
GRANT ALL ON SCHEMA public TO public;
GRANT ALL PRIVILEGES ON DATABASE enterprise_shop_dev TO shop_dev;
ALTER SCHEMA public OWNER TO shop_dev;
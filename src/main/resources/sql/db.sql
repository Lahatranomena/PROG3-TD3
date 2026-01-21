create database "mini_dish_db";

create user "mini_dish_db_manager" with password '123456';

-- Grant all privileges

-- \c mini_dish_db;

--GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;
--GRANT CREATE ON DATABASE mini_dish_db TO mini_dish_db_manager;
--GRANT USAGE, CREATE ON SCHEMA public TO mini_dish_db_manager;
--ALTER DEFAULT PRIVILEGES IN SCHEMA public
    --GRANT SELECT, INSERT,UPDATE, DELETE ON tables TO mini_dish_db_manager;

--ALTER DEFAULT PRIVILEGES IN SCHEMA public
    --GRANT USAGE, SELECT, UPDATE ON sequences TO mini_dish_db_manager;
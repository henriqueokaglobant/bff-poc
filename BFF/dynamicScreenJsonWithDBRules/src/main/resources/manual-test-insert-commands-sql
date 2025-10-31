docker exec -it bff-postgres psql -U postgres -d postgres -c "SELECT template, name, title_key, component_type FROM component;"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into component(template, name, title_key, component_type, exclude_by_default) values('user_profile', 'newfield', '@@newLabel@@', 'input', FALSE);"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into component(template, name, title_key, component_type, exclude_by_default) values('user_profile', 'newfield2', '@@newLabel2@@', 'input', FALSE);"

docker exec -it bff-postgres psql -U postgres -d postgres -c "SELECT * FROM localization;"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into localization(locale, message_key, message_value) values('en_US', 'newLabel', 'This is a new label');"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into localization(locale, message_key, message_value) values('en_US', 'newLabel2', 'This is another new label');"

docker exec -it bff-postgres psql -U postgres -d postgres -c "SELECT * FROM rule;"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into rule(template, property_name, property_value, component_name, order_priority, include) values('user_profile', 'role', 'admin', 'newfield', 0, null);"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into rule(template, property_name, property_value, component_name, order_priority, include) values('user_profile', 'role', 'admin', 'newfield2', 0, null);"

docker exec -it bff-postgres psql -U postgres -d postgres -c "insert into rule(template, property_name, property_value, component_name, order_priority, include) values('user_profile', 'role', 'support', 'newfield', null, FALSE);"

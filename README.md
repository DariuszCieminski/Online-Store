## Swagger Example
This project represents an online store, consists of back-end part made in Spring Boot and front-end part written in Angular. As the method of user authentication, it uses [JSON Web Token (JWT)](https://jwt.io/). API endpoints and model classes are documented using Springfox Swagger library.
The application is available [HERE](https://pl-swaggerexample.netlify.app/).

Application uses following technologies:
 - Spring Boot 2.3
 - Hibernate 5.4
 - Springfox 3.0.0
 - Angular 10

There are 3 user roles:
 1. User (regular client)
 2. Manager (administrator, can modify users and products)
 3. Developer (has access to the Swagger UI)

Some features:
 - integration with Spring Security
 - data validation using Hibernate Validator
 - filtering products by multiple predicates
 - browsing orders with ability to sort and search

## To do
 - Administrator panel to manage users/orders by the UI

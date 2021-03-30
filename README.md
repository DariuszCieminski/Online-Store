# Swagger Example
This project represents an online store, consists of back-end part made in Spring Boot and front-end part written in Angular.
It uses two user authentication methods, session and [JSON Web Token (JWT)](https://jwt.io/) with different application profiles for each.
API endpoints and model classes are well documented using Springfox Swagger library.
The working application is available [HERE](https://pl-swaggerexample.netlify.app/).

Application uses following technologies:
 - Spring Boot 2.3
 - Hibernate 5.4
 - Springfox 3.0.0
 - Angular 10

There are 3 user roles:
 1. User (regular client)
 2. Manager (administrator, can modify users and products)
 3. Developer (has access to the Swagger UI)

Some application features:
 - integration with Spring Security
 - data validation using Hibernate Validator
 - filtering products by multiple predicates
 - browsing orders with ability to sort and search

## To do
 - Administrator panel to manage users/orders by the UI
 
# Running the application
The application starts in 'jwt' profile by default in both backend and frontend modules with preconfigured H2 database.

## Backend
Please make sure you have Maven installed.<br><br>
To change the default database, one needs to modify pom.xml file (to add another JDBC driver)
and application.properties file (to configure URL and login credentials).<br><br>
To start the application with default profile, please use the following command:

```
mvn clean spring-boot:run
```
or
```
mvn clean spring-boot:run -P jwt
```

If the application should run in session authentication mode, please use:

```
mvn clean spring-boot:run -P session
```

For packaging the application into a JAR file (to be able to use it as a Docker image for example), use this command:

```
mvn clean package
```

and for session profile:

```
mvn clean package -P session
```

## Frontend
Make sure you have Angular CLI installed.<br><br>
To start the frontend module type:

```
npm start
```

To run with session authentication profile:

```
npm start:session
```

When compiling the application into static html/js files, which can be read directly by any web browser please use:

```
npm run build
npm run build:session
```
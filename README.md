# Online Store
This project represents an online store, consists of back-end part made in Spring Boot and front-end part written in Angular.
It uses two user authentication methods, HTTP session and [JSON Web Token (JWT)](https://jwt.io/) with separate application profiles for each mode.<br>
API endpoints and model classes are well documented using Springfox Swagger library.<br>
The working demo application is available [HERE](https://pl-onlinestore.netlify.app/).

Application uses following technologies:
 - Spring Boot
 - Hibernate
 - Springfox
 - Angular

There are 3 user roles:
 1. User (regular store customer)
 2. Manager (administrator, can modify users and products)
 3. Developer (has access to the Swagger UI)

Some application features:
 - integration with Spring Security
 - data validation using Hibernate Validator
 - filtering products by multiple predicates
 - browsing your orders with ability to sort and search

## To do
 - Administrator panel to manage users/orders by the UI
 
# Running the application
The application starts in 'jwt' profile by default in both backend and frontend modules.

## Docker
The easiest way is to use Docker, as this project has already configured docker-compose file,
as well as dockerfiles for building backend and frontend modules.<br><br>
You can modify any application property in the `.env` config file.<br><br>
To start the application, run the following command in project's main folder:

```
docker-compose up
```

This will build Docker images for all app modules (DB, Frontend and Backend) and start them as separate containers connected to shared network.<br><br>
After all modules finish loading, type in a web browser `http://localhost:4200` to open main application page.<br><br>
To stop running containers, type in the console:

```
docker-compose down
```

## Without Docker
To run each application module individually please use the instructions below.

### Backend
Make sure you have Maven installed.<br><br>
To configure the default PostgreSQL database, one needs to modify `application.properties` file and configure URL and login credentials.<br><br>
If there is a need to change Postgres to other DB, `pom.xml` file will also need modifications to replace the JDBC driver.

To start the application with default profile, use the following command:

```
mvn clean spring-boot:run
```

which is equivalent to:

```
mvn clean spring-boot:run -P jwt
```

If the application should run in session authentication mode, please use:

```
mvn clean spring-boot:run -P session
```

To package the application into a JAR file, type:

```
mvn clean package
```

and for session profile:

```
mvn clean package -P session
```

### Frontend
Make sure you have Angular CLI installed.<br><br>
First step is to set your Backend API URL by modifying the `BASE_URL` variable inside the following file:

```
/frontend/src/app/util/api-urls.ts
```

To build the frontend module type:

```
npm run build:jwt
```

and for session profile:

```
npm run build:session
```

It creates a new `dist` folder containing compiled Angular application into static files.<br>
These can be run using any server like NGINX or PM2 process manager.
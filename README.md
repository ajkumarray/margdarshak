# Margdarshak - URL Shortener Service

A robust and scalable URL shortening service built with Spring Boot.

## Features

- Create short URLs for long URLs
- Automatic URL expiration
- URL statistics tracking
- URL enable/disable functionality
- RESTful API endpoints
- Swagger/OpenAPI documentation
- Security features (CORS, XSS protection)
- Health monitoring

## Technical Stack

- Java 17
- Spring Boot 3.x
- PostgreSQL
- Maven
- Swagger/OpenAPI 3.0

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL 12 or higher

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/margdarshak.git
cd margdarshak
```

2. Configure the database:
- Create a PostgreSQL database named `margdarshak`
- Update the database credentials in `application.properties`

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## API Documentation

Swagger UI is available at `http://localhost:8080/swagger-ui.html`

### Endpoints

#### Create Short URL
- **POST** `/api/v1/urls`
- Request body:
```json
{
    "url": "https://example.com",
    "expirationDays": 30
}
```

#### Redirect to Original URL
- **GET** `/api/v1/urls/{shortUrl}`

#### Get URL Statistics
- **GET** `/api/v1/urls/{shortUrl}/stats`

#### Update URL Expiration
- **PUT** `/api/v1/urls/{shortUrl}?expirationDays=14`

#### Disable URL
- **POST** `/api/v1/urls/{shortUrl}/disable`

#### Enable URL
- **POST** `/api/v1/urls/{shortUrl}/enable`

## Security Considerations

- CORS is configured to allow requests from specific origins
- XSS protection is enabled
- Content Security Policy is implemented
- Input validation is performed on all endpoints
- Rate limiting is recommended for production use

## Monitoring

The application includes Actuator endpoints for monitoring:
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`

## Development

### Code Organization

- `com.ajkumarray.margdarshak.controller`: REST API endpoints
- `com.ajkumarray.margdarshak.service`: Business logic
- `com.ajkumarray.margdarshak.repository`: Data access layer
- `com.ajkumarray.margdarshak.entity`: Database entities
- `com.ajkumarray.margdarshak.dto`: Data transfer objects
- `com.ajkumarray.margdarshak.exception`: Custom exceptions
- `com.ajkumarray.margdarshak.validator`: Input validation
- `com.ajkumarray.margdarshak.config`: Configuration classes
- `com.ajkumarray.margdarshak.constants`: Application constants
- `com.ajkumarray.margdarshak.util`: Utility classes

### Testing

Run tests with:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
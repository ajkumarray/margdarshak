# Margdarshak - URL Shortener Service

A robust and scalable URL shortening service built with Spring Boot, featuring secure authentication and comprehensive URL management capabilities.

## Features

- **URL Management**
  - Create short URLs for long URLs
  - Automatic URL expiration
  - URL statistics tracking
  - URL enable/disable functionality
  - Bulk URL management

- **Security**
  - JWT-based authentication
  - User registration and management
  - CORS protection
  - XSS protection
  - Content Security Policy
  - Rate limiting support
  - Password encryption using BCrypt

- **API & Documentation**
  - RESTful API endpoints
  - Swagger/OpenAPI documentation
  - Comprehensive API versioning
  - Detailed error handling

- **Monitoring & Maintenance**
  - Health monitoring
  - Performance metrics
  - Detailed logging
  - Database statistics

## Technical Stack

- **Backend**
  - Java 17
  - Spring Boot 3.x
  - Spring Security
  - Spring Data JPA
  - PostgreSQL
  - Maven

- **Tools & Libraries**
  - Swagger/OpenAPI 3.0
  - JWT Authentication
  - Lombok
  - Spring Actuator
  - Spring Cache

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL 12 or higher
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/margdarshak.git
cd margdarshak
```

2. Configure the database:
   - Create a PostgreSQL database named `margdarshak`
   - Update the database credentials in `application.properties`:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/margdarshak
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

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

### Authentication

#### Register User
- **POST** `/api/v1/auth/register`
- Request body:
```json
{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securepassword",
    "profilePic": "https://example.com/profile.jpg"
}
```

#### Login
- **POST** `/api/v1/auth/login`
- Request body:
```json
{
    "email": "john@example.com",
    "password": "securepassword"
}
```

### URL Management

#### Create Short URL
- **POST** `/api/v1/url`
- Headers: `Authorization: Bearer <jwt_token>`
- Request body:
```json
{
    "url": "https://example.com",
    "expirationDays": 30
}
```

#### Get All URLs
- **GET** `/api/v1/url`
- Headers: `Authorization: Bearer <jwt_token>`

#### Get URL Details
- **GET** `/api/v1/url/detail/{code}`
- Headers: `Authorization: Bearer <jwt_token>`

#### Update URL
- **PUT** `/api/v1/url/update/{code}`
- Headers: `Authorization: Bearer <jwt_token>`
- Request body:
```json
{
    "url": "https://updated-example.com",
    "expirationDays": 14
}
```

### URL Redirection

#### Redirect to Original URL
- **GET** `/{code}`
- No authentication required
- Returns 302 redirect to original URL

## Security

### Authentication
- JWT-based token authentication
- Token expiration and refresh mechanism
- Secure password storage with BCrypt
- Role-based access control

### Protection
- CORS configuration for specific origins
- XSS protection headers
- Content Security Policy
- Input validation and sanitization
- Rate limiting support
- Secure session management

## Monitoring

The application includes Actuator endpoints for monitoring:
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`
- Environment: `/actuator/env`
- Mappings: `/actuator/mappings`

## Development

### Code Organization

- `com.ajkumarray.margdarshak.controller`: REST API endpoints
- `com.ajkumarray.margdarshak.service`: Business logic
- `com.ajkumarray.margdarshak.repository`: Data access layer
- `com.ajkumarray.margdarshak.entity`: Database entities
- `com.ajkumarray.margdarshak.models.request`: Request DTOs
- `com.ajkumarray.margdarshak.models.response`: Response DTOs
- `com.ajkumarray.margdarshak.exception`: Custom exceptions
- `com.ajkumarray.margdarshak.validator`: Input validation
- `com.ajkumarray.margdarshak.config`: Configuration classes
- `com.ajkumarray.margdarshak.constants`: Application constants
- `com.ajkumarray.margdarshak.util`: Utility classes
- `com.ajkumarray.margdarshak.enums`: Enumeration classes
- `com.ajkumarray.margdarshak.security`: Security configuration and JWT handling

### Testing

Run tests with:
```bash
mvn test
```

### Docker Support

Build and run using Docker:
```bash
# Build the image
docker build -t margdarshak .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/margdarshak \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  margdarshak
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please open an issue in the GitHub repository or contact the maintainers. 
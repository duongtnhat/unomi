# Deployment

Build and run the app with all local dependencies:

```bash
docker compose -f deployment/docker-compose.yml up -d --build
```

The application is exposed at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Stop the stack:

```bash
docker compose -f deployment/docker-compose.yml down
```

Remove persistent volumes:

```bash
docker compose -f deployment/docker-compose.yml down -v
```

Render or install the Kubernetes chart:

```bash
helm template unomi deployment/helm/unomi
helm upgrade --install unomi deployment/helm/unomi
```

docker network create portal

docker run -d --name postgres --network portal `
  -e POSTGRES_DB=developer_portal -e POSTGRES_USER=developer_portal -e POSTGRES_PASSWORD=secret `
  postgres:17

docker run --rm --network portal -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=dev `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/developer_portal `
  -e SPRING_DATASOURCE_USERNAME=developer_portal `
  -e SPRING_DATASOURCE_PASSWORD=secret `
  developer-portal:local
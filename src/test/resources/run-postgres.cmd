docker run --name dev-postgres \
  -e POSTGRES_DB=developerportal \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:17
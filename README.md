# Batch processing jdbc

Examples for 
- `@EnableBatchProcessing` job and step builder
- DAO pattern using jdbcTemplate / Prepared statements

# Setup

## Docker
```sh
docker pull mysql
docker run --name some-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql
docker network create practice-db
docker network connect some-mysql practice-db
docker run -it --network practice-db --rm mysql mysql -hsome-mysql -uroot -p

CREATE DATABASE dbname;
```

## Reference
https://spring.io/guides/gs/batch-processing/


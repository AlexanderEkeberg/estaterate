CREATE TABLE crime_area_stats (
    id              BIGSERIAL PRIMARY KEY,
    area_code       VARCHAR(10)      NOT NULL,
    area_name       VARCHAR(100)     NOT NULL,
    area_type       VARCHAR(20)      NOT NULL,
    source          VARCHAR(50)      NOT NULL,
    year            INT              NOT NULL,
    crime_count     INT              NOT NULL,
    population      INT              NOT NULL,
    rate_per_1000   DOUBLE PRECISION NOT NULL,
    score           DOUBLE PRECISION NOT NULL,

    CONSTRAINT uq_crime_area_stats_area_year UNIQUE (area_code, area_type, year)
);

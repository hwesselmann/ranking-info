--liquibase formatted sql

--changeset hwesselmann:test-001-ranking-testdata context:test dbms:h2
insert into ranking (id, dtbid, firstname, lastname, federation, club, nationality, period, gender, yob, points, rankOverall) values (1, 10812345, 'Max', 'Mustermann', 'WTV', 'TC Musterhausen', 'GER', '2020-03-31', true, 2008, '21,3', 1884);
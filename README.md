## ranking-info  
![Main](https://github.com/hwesselmann/ranking-info/workflows/Test/badge.svg)

This is a small web-application aiming at providing accessible information on the German national tennis youth rankings.

### Prerequisites  

This application is developed using

* Ruby 2.6.6

* Rails 6.0.2.2

* SQLite3

### Database setup  

To setup a developement database, check config/database.yml and then run __rake db:migrate__

### Tests

To run the test suites, run __rake test__

### Deploy  

If you want to deploy the application, you need to set some environment variables in order to fill required
information for the imprint on the about page. You can also hardcode the values in config/config.yml.

* DOMAIN - the domain the application is deployed to
* IMPRINT_NAME - name for the imprint
* IMPRINT_STREET - street and street number for the imprint
* IMPRINT_ZIPCODE - zip code for imprint
* IMPRINT_CITY - city for imprint
* IMPRINT_PHONE - phone number for imprint
* IMPRINT_MAIL - email address for imprint
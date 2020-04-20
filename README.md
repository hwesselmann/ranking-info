## ranking-info  
![Main](https://github.com/hwesselmann/ranking-info/workflows/Test/badge.svg) [![Test Coverage](https://api.codeclimate.com/v1/badges/bdc191d3fb1a692f0603/test_coverage)](https://codeclimate.com/github/hwesselmann/ranking-info/test_coverage) [![Maintainability](https://api.codeclimate.com/v1/badges/bdc191d3fb1a692f0603/maintainability)](https://codeclimate.com/github/hwesselmann/ranking-info/maintainability)

This is a small web-application aiming at providing accessible information on the German national tennis youth rankings.

### Prerequisites  

This application is developed using

* Ruby 2.6.6
* Rails 6.0.2.2
* SQLite3 (for development and test)
* bundler package manager (for rubygems)
* Yarn package manager (for web assets)

### Setup  

To setup a developement environment, run the following steps before starting _rails server_   

> \# run database migrations  
> rake db:migrate  
> \# install bundle  
> bundle install  
> \# install web assets  
> yarn install --update-files

Now everything is ready for starting a development server!

### Tests

To run the test suites, run __rake test__  

The project also includes guard and an guard configuration. To start guard and run tests automatically on code changes, simply open a terminal and start __guard__ while in the project folder.

### Import file format  

To import data in the system, you need to create a csv file from the official ranking list pdf using a tool like
tabula. The file needs to be in the format  

> ranking-position, lastname, firstname, nationality, dtb_id federation, club, score

which is the standard order and format tabula (which I am using) is retrieving by default.
An example file can be found in _test/fixtures/files_

### Importing data / login credentials  

The system has an easy mechanism to upload csv-files for importing into the database. Access to this funtionality is, however, restricted. In order to use it, you need to add a user into the database manually - there is no way of editing this on the ui:

 1. make sure your database ist up to date by running `rake db:migrate`
 2. open a rails console by running `rails console`
 3. Add a user by running `User.create(name: 'Example User', email: 'user@example.com', password: 'password', password_confirmation: 'password'` with your preferred data

The password cannot be empty and should be at least 6 characters!

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
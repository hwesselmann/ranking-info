# build stage
FROM ruby:2.6.6-alpine as build

RUN apk add --update --no-cache \
    build-base \
    postgresql-dev \
    git \
    imagemagick \
    nodejs-current \
    yarn \
    tzdata \
    sqlite-dev \
    python2 \
    make \
    g++

WORKDIR /app

ENV SECRET_KEY_BASE rankingInfo

# Install gems
COPY Gemfile* /app/
RUN gem install bundler
RUN bundle config --global frozen true \
 && bundle config --local without development:test \
 && bundle install --jobs=4 \
 # Remove unneeded files (cached *.gem, *.o, *.c)
 && rm -rf /usr/local/bundle/cache/*.gem \
 && find /usr/local/bundle/gems/ -name "*.c" -delete \
 && find /usr/local/bundle/gems/ -name "*.o" -delete

# Install yarn packages
COPY package.json yarn.lock /app/
RUN yarn install

# Add the Rails app
COPY . /app

# Precompile assets
RUN RAILS_ENV=production bundle exec rake assets:precompile

# Remove folders not needed in resulting image
RUN rm -rf node_modules tmp/cache vendor/assets lib/assets spec

# final image
FROM ruby:2.6.6-alpine
LABEL maintainer="hauke@h-dawg.de"

# Add Alpine packages
RUN apk add --update --no-cache \
    postgresql-client \
    imagemagick \
    tzdata \
    file

# Copy app with gems from former build stage
COPY --from=build /usr/local/bundle/ /usr/local/bundle/
COPY --from=build /app /app

WORKDIR /app

RUN mkdir public/uploads \
  && chmod 777 public/uploads

ENV SECRET_KEY_BASE rankingInfo

# Save timestamp of image building
RUN date -u > BUILD_TIME

EXPOSE 3000

CMD rm -f tmp/pids/server.pid \
  && bundle exec rails db:migrate \
  && bundle exec rails s -b 0.0.0.0 -p 3000

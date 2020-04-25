FROM ruby:2.6.6-slim

# https://yarnpkg.com/lang/en/docs/install/#debian-stable
RUN apt-get update -qq && apt-get install -y curl gnupg2 nodejs postgresql-client libpq-dev && \
  curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - && \
  echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list && \
  apt-get update -qq && apt-get install -y build-essential patch zlib1g-dev liblzma-dev && \
  apt-get install -y yarn imagemagick

ENV APP_PATH=/app
RUN mkdir $APP_PATH
WORKDIR $APP_PATH

COPY . /app

RUN gem install bundler && \
  bundle config set with 'production' && \
  bundle config set without 'development test' && \
  bundle install

COPY entrypoint.sh /usr/bin/
RUN chmod +x /usr/bin/entrypoint.sh
ENTRYPOINT ["entrypoint.sh"]
EXPOSE 3000

CMD ["rails", "server", "-b", "0.0.0.0"]

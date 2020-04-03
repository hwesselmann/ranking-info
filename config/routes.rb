Rails.application.routes.draw do
  root 'static_pages#home'
  get '/about', to: 'static_pages#about'
  get '/status', to: 'import#info'
  post '/import', to: 'import#import'
  get '/players', to: 'players#index'
  get '/player/:id', to: 'players#show', as: :player
  resources :import do
    collection { post :import }
  end
end

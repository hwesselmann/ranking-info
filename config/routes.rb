Rails.application.routes.draw do
  root 'static_pages#home'
  get '/about', to: 'static_pages#about'
  get '/status', to: 'import#info'
  get '/import', to: 'import#upload'
  post '/import', to: 'import#import'
  get '/players', to: 'players#index'
  get '/player/:id', to: 'players#show', as: :player
  get '/listings', to: 'listing#index'
  get '/login', to: 'sessions#new'
  post '/login', to: 'sessions#create'
  delete '/logout', to: 'sessions#destroy'
  resources :import do
    collection { post :import }
  end
end

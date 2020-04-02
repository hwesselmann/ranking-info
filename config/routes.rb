Rails.application.routes.draw do
  root 'static_pages#home'
  get 'static_pages/home'
  get 'static_pages/about'
  get 'import/info'
  get 'import/import'
  resources :import do
    collection { post :import }
  end
  resources :players, only: [:show]
end

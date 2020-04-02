Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  root 'import#info'
  get 'import/info'
  get 'import/import'
  resources :import do
    collection { post :import }
  end
  resources :players, only: [:show]
end

class Ranking < ApplicationRecord
  belongs_to :player, primary_key: 'dtb_id', foreign_key: 'dtb_id'
end

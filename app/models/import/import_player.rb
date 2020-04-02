# frozen_string_literal: true

#
# Model for a player on the ranking.
# Attributes:
#  Integer @dtb_id
#  Integer @current_ranking
#  String @current_score
#  String @firstname
#  String @lastname
#  String @federation
#  String @club
#  String @nationality
#  Array @clubs
#  Array @rankings
#
class ImportPlayer
  attr_accessor :dtb_id, :current_ranking, :current_score, :lastname, :firstname
  attr_accessor :federation, :club, :nationality, :clubs, :rankings
end

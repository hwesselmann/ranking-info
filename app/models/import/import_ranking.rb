# frozen_string_literal: true

#
# Model representing a ranking entry for a player.
# Attributes:
#  Integer @dtb_id
#  Time @date
#  String @age_group
#  Integer @ranking_position
#  String @score
#  Boolean @age_group_ranking
#  Boolean @yob_ranking
#  Boolean @year_end_ranking
#
class ImportRanking
  def initialize(dtb_id: '', date: '', age_group: '',
                 score: '', ranking_position: '')
    @dtb_id = dtb_id
    @date = date
    @age_group = age_group
    @score = score
    @ranking_position = ranking_position
  end

  attr_accessor :dtb_id, :date, :age_group, :ranking_position, :score
  attr_accessor :age_group_ranking, :yob_ranking, :year_end_ranking
end

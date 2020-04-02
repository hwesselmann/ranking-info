require 'test_helper'
class PlayerTest < ActiveSupport::TestCase

  def setup
    @player = Player.new
  end

  test 'period extraction from filename' do
    sut = @player.extract_period_from_filename('Junioren_20180331.csv')
    assert_equal(2018, sut.year)
    assert_equal(3, sut.month)
    assert_equal(31, sut.day)

    sut = @player.extract_period_from_filename('Junioren_20121231.csv')
    assert_equal(2012, sut.year)
    assert_equal(12, sut.month)
    assert_equal(31, sut.day)
  end

  test 'throw exception for invalid period extraction' do
    exception = assert_raises RuntimeError do
      @player.extract_period_from_filename('Junioren-20180331.csv')
    end
    exp = "could not retrieve period part from filename 'Junioren-20180331.csv'"
    assert_equal(exp, exception.message)
  end

  test 'import of players from file' do
    sut = @player.read_players_from_csv('./test/fixtures/files/Junioren_20180331.csv')
    assert_equal(17, sut.size)
    assert_instance_of(ImportPlayer, sut.fetch(10))
    assert_equal('Eintracht Frankfurt', sut.fetch(0).club)
    assert_equal('J****n', sut.fetch(0).firstname)
    assert_equal('P****y', sut.fetch(0).lastname)
    assert_equal(173, sut.fetch(16).current_ranking)
    assert_equal('N***h', sut.fetch(16).firstname)
    assert_equal('W****n', sut.fetch(16).lastname)
    assert_equal(10_522_076, sut.fetch(16).dtb_id)
  end

  test 'filling ranking' do
    player = ImportPlayer.new
    player.dtb_id = 10_812_345
    player.current_ranking = 199
    player.current_score = '23,6'
    period_end = Time.new('2019', '3', '31')
    sut = @player.fill_ranking_for_player('Overall', period_end, player)

    assert_instance_of(ImportRanking, sut)
    assert_equal(199, sut.ranking_position)
    assert_equal(10_812_345, sut.dtb_id)
    assert_equal('Overall', sut.age_group)
    assert_equal('23,6', sut.score)
    assert_equal(period_end.day, sut.date.day)
    assert_equal(period_end.year, sut.date.year)
  end

  test 'mapping and syncing player data' do
    period_end = Time.new('2019', '3', '31')

    player = ImportPlayer.new
    player.lastname = 'Mustermann'
    player.firstname = 'Max'
    player.dtb_id = 10_712_345
    player.nationality = 'GER'
    player.federation = 'WTV'
    player.club = 'TC Musterhausen'
    player.current_ranking = 199
    player.current_score = '45,8'

    already_in_system = ImportPlayer.new
    already_in_system.lastname = 'Mustermann'
    already_in_system.firstname = 'Max'
    already_in_system.dtb_id = 10_712_345
    already_in_system.nationality = 'GER'
    already_in_system.federation = 'WTV'
    already_in_system.club = 'TC Musterhausen'
    already_in_system.current_ranking = 283
    already_in_system.current_score = '15'
    ranking = ImportRanking.new
    ranking.age_group = 'Overall'
    ranking.date = Time.new('2018', '12', '31')
    ranking.ranking_position = 283
    ranking.age_group_ranking = 'U12'
    ranking.score = '15'
    ranking.dtb_id = already_in_system.dtb_id
    rankings = []
    rankings.push(ranking)
    already_in_system.rankings = rankings

    # case 1 new ranking not already imported
    sut = @player.map_and_sync_player(player, already_in_system, period_end)
    assert_equal(already_in_system.lastname, sut.lastname)
    assert_equal(already_in_system.firstname, sut.firstname)
    assert_equal(already_in_system.club, sut.club)
    assert_equal(player.current_ranking, sut.current_ranking)
    refute_equal(already_in_system.current_ranking, sut.current_ranking)
    assert_equal(2, sut.rankings.size)

    # case 2 ranking has already been imported before
    ranking = ImportRanking.new
    ranking.age_group = 'Overall'
    ranking.date = period_end
    ranking.ranking_position = player.current_ranking
    ranking.age_group = 'U12'
    ranking.score = player.current_score
    ranking.dtb_id = player.dtb_id
    already_in_system.rankings.push(ranking)
    player.rankings = nil
    sut = @player.map_and_sync_player(player, already_in_system, period_end)
    assert_instance_of(ImportPlayer, sut)
    assert_equal(already_in_system.lastname, sut.lastname)
    assert_equal(already_in_system.firstname, sut.firstname)
    assert_equal(already_in_system.club, sut.club)
    refute_equal(already_in_system.current_ranking, sut.current_ranking)
    assert_equal(2, sut.rankings.size)
    assert_instance_of(ImportRanking, sut.rankings.fetch(1))
    assert_equal(period_end, sut.rankings.fetch(1).date)

    # case 3 new club
    already_in_system.club = 'Musterhausener TV'
    sut = @player.map_and_sync_player(player, already_in_system, period_end)
    assert_instance_of(ImportPlayer, sut)
    assert_equal(player.club, sut.club)
    assert_equal(1, sut.clubs.size)
    assert_instance_of(String, sut.clubs.fetch(0))
    assert_equal(already_in_system.club, sut.clubs.fetch(0))
  end

  test 'calculation of yob to fetch from db' do
    sut = @player.calculate_yob_range_to_fetch('2008', 12, Time.new('2019', '03', '31'), 100)
    assert_equal(108, sut.fetch(0))
    assert_equal(107, sut.fetch(1))
    assert_equal(2, sut.size)

    sut = @player.calculate_yob_range_to_fetch('2008', 14, Time.new('2019', '03', '31'), 200)
    assert_equal(208, sut.fetch(0))
    assert_equal(206, sut.fetch(2))
    assert_equal(4, sut.size)
  end

  test 'sorting of rankings' do
    in_rankings = []
    rank = 53
    i = 0
    while i < 25
      ranking = ImportRanking.new
      ranking.dtb_id = 12_345_678
      ranking.age_group = 'Overall'
      ranking.yob_ranking = false
      ranking.age_group_ranking = false
      if (i % 3).zero?
        ranking.ranking_position = rank
      else
        rank += i
        ranking.ranking_position = rank
      end
      in_rankings.push(ranking)
      i += 1
    end
    sut = @player.sort_rankings_for_age_group(in_rankings, 11, true, false)

    assert_instance_of(ImportRanking, sut.fetch(0))
    assert_equal(1, sut.fetch(0).ranking_position)
    last_rank = 0
    sut.each do |entry|
      assert_instance_of(ImportRanking, entry)
      assert(entry.ranking_position >= last_rank)
      assert_equal('U11', entry.age_group)
      assert(entry.yob_ranking)
      refute(entry.age_group_ranking)
      refute_equal(entry.ranking_position, in_rankings.fetch(sut.index(entry)))
      last_rank = entry.ranking_position
    end
  end

  test 'full ranking_file_import' do
    assert(File.exist?('./test/fixtures/files/Junioren_20180331.csv'))
    assert_equal(2, Player.count)
    assert_equal(4, Ranking.count)
    assert_equal(3, Club.count)
    @player.import_rankings('./test/fixtures/files/Junioren_20180331.csv')
    assert_equal(143, Ranking.count)
    assert_equal(19, Player.count)
    assert_equal(4, Club.count)
  end
end

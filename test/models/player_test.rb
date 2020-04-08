require 'test_helper'
class PlayerTest < ActiveSupport::TestCase
  test 'period extraction from filename' do
    sut = Player.extract_period_from_filename('Junioren_20180331.csv')
    assert_equal(2018, sut.year)
    assert_equal(3, sut.month)
    assert_equal(31, sut.day)

    sut = Player.extract_period_from_filename('Junioren_20121231.csv')
    assert_equal(2012, sut.year)
    assert_equal(12, sut.month)
    assert_equal(31, sut.day)
  end

  test 'throw exception for invalid period extraction' do
    exception = assert_raises RuntimeError do
      Player.extract_period_from_filename('Junioren-20180331.csv')
    end
    exp = "could not retrieve period part from filename 'Junioren-20180331.csv'"
    assert_equal(exp, exception.message)
  end

  test 'import of players from file' do
    sut = Player.read_players_from_csv('./test/fixtures/files/Junioren_20180331.csv')
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
    sut = Player.fill_ranking_for_player('Overall', period_end, player)

    assert_instance_of(ImportRanking, sut)
    assert_equal(199, sut.ranking_position)
    assert_equal(10_812_345, sut.dtb_id)
    assert_equal('Overall', sut.age_group)
    assert_equal('23,6', sut.score)
    assert_equal(period_end.day, sut.date.day)
    assert_equal(period_end.year, sut.date.year)
  end

  test 'calculation of yob to fetch from db' do
    sut = Player.calculate_yob_range_to_fetch('2008', 11, Time.new('2019', '03', '31'), 100)
    assert_equal(108, sut.fetch(0))
    assert_equal(1, sut.size)
    
    sut = Player.calculate_yob_range_to_fetch('2008', 12, Time.new('2019', '03', '31'), 100)
    assert_equal(108, sut.fetch(0))
    assert_equal(107, sut.fetch(1))
    assert_equal(2, sut.size)

    sut = Player.calculate_yob_range_to_fetch('2008', 14, Time.new('2019', '03', '31'), 200)
    assert_equal(208, sut.fetch(0))
    assert_equal(206, sut.fetch(2))
    assert_equal(4, sut.size)

    sut = Player.calculate_yob_range_to_fetch('2008', 16, Time.new('2019', '03', '31'), 200)
    assert_equal(208, sut.fetch(0))
    assert_equal(204, sut.fetch(4))
    assert_equal(6, sut.size)
  end

  test 'sorting of rankings' do
    in_rankings = []
    rank = 53
    i = 0
    while i < 25
      ranking = ImportRanking.new
      ranking.dtb_id = 10_712_345
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
    sut = Player.sort_rankings_for_age_group(in_rankings, 11, true, false, false)

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
    Player.import_rankings('./test/fixtures/files/Junioren_20180331.csv')
    assert_equal(148, Ranking.count)
    assert_equal(18, Player.count)
    assert_equal(4, Club.count)
  end
end

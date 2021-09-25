# frozen_string_literal: true

require 'test_helper'

class RankingTest < ActiveSupport::TestCase
  test 'period extraction from filename' do
    sut = Ranking.extract_period_from_filename('Junioren_20180331.csv')
    assert_equal(Time.new('2018', '3', '31'), sut)

    sut = Ranking.extract_period_from_filename('Junioren_20121231.csv')
    assert_equal(Time.new('2012', '12', '31'), sut)
  end

  test 'throw exception for invalid period extraction' do
    exception = assert_raises RuntimeError do
      Ranking.extract_period_from_filename('Junioren-20180331.csv')
    end
    exp = "could not retrieve period part from filename 'Junioren-20180331.csv'"
    assert_equal(exp, exception.message)
  end

  test 'calculation of yob to fetch from db' do
    date = Time.new('2019', '03', '31')
    sut = Ranking.yob_range_to_fetch('2008', 11, date, 100)
    assert_equal(108, sut.fetch(0))
    assert_equal(1, sut.size)

    sut = Ranking.yob_range_to_fetch('2008', 12, date, 100)
    assert_equal(108, sut.fetch(0))
    assert_equal(107, sut.fetch(1))
    assert_equal(2, sut.size)

    sut = Ranking.yob_range_to_fetch('2008', 14, date, 200)
    assert_equal(208, sut.fetch(0))
    assert_equal(206, sut.fetch(2))
    assert_equal(4, sut.size)

    sut = Ranking.yob_range_to_fetch('2008', 16, date, 200)
    assert_equal(208, sut.fetch(0))
    assert_equal(204, sut.fetch(4))
    assert_equal(6, sut.size)
  end

  test 'full ranking_file_import' do
    assert(File.exist?('./test/fixtures/files/Junioren_20180401.csv'))
    assert(File.exist?('./test/fixtures/files/Juniorinnen_20180401.csv'))
    assert_equal(4, Ranking.count)
    Ranking.import_rankings('./test/fixtures/files/Junioren_20180401.csv')
    Ranking.import_rankings('./test/fixtures/files/Juniorinnen_20180401.csv')
    assert_equal(292, Ranking.count)
  end
end

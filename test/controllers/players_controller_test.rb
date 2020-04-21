require 'test_helper'

class PlayersControllerTest < ActionDispatch::IntegrationTest
  test 'should get players' do
    get players_path
    assert_response :success
    assert_select 'title', full_title('Spielersuche')
  end

  test 'should get player profile' do
    get player_path(10_711_111)
    assert_response :success
    assert_select 'title', full_title('Spielerprofil J****n P****y')
  end

  test 'filling up dtb_id' do
    sut = PlayersController.new

    assert_equal(10_812_345, sut.fill_up_dtb_id(10_812_345))
    assert_equal(10_000_000, sut.fill_up_dtb_id(1))
    assert_equal(10_000_000, sut.fill_up_dtb_id(10))
    assert_equal(20_800_000, sut.fill_up_dtb_id(208))
  end

  test 'filling up dtb_id end' do
    sut = PlayersController.new

    assert_equal(10_812_345, sut.fill_up_dtb_id_end(10_812_345))
    assert_equal(19_999_999, sut.fill_up_dtb_id_end(1))
    assert_equal(10_999_999, sut.fill_up_dtb_id_end(10))
    assert_equal(20_899_999, sut.fill_up_dtb_id_end(208))
  end
end

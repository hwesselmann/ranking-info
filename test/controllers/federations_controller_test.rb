require 'test_helper'

class FederationsControllerTest < ActionDispatch::IntegrationTest
  test 'should get index' do
    get federations_path
    assert_response :success
    assert_select 'title', full_title('Verbandsdaten')
  end

  test 'get current quarter' do
    sut = FederationsController.new
    assert_instance_of(Ranking, sut.current_quarter)
    assert_equal('2018-07-01', sut.current_quarter.date.strftime('%Y-%m-%d'))
  end

  test 'get player count' do
    sut = FederationsController.new
    federations = sut.player_count_by_federation(sut.current_quarter, 'm')
    assert_equal(0, federations.count)
    federations = sut.player_count_by_federation(sut.current_quarter, 'w')
    assert_equal(0, federations.count)
  end
end

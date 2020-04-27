require 'test_helper'

class FederationsControllerTest < ActionDispatch::IntegrationTest
  test 'get current quarter' do
    sut = FederationsController.new
    assert_instance_of(Ranking, sut.current_quarter)
    assert_equal('2018-07-01', sut.current_quarter.date.strftime('%Y-%m-%d'))
  end
end

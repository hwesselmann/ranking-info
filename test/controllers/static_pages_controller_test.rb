require 'test_helper'

class StaticPagesControllerTest < ActionDispatch::IntegrationTest
  def setup
    @base_title = 'Ranking-Info'
  end

  test 'should get home' do
    get root_path
    assert_response :success
    assert_select 'title', full_title('')
  end

  test 'should get help' do
    get help_path
    assert_response :success
    assert_select 'title', full_title('Hilfe')
  end
end

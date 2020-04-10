require 'test_helper'

class ApplicationHelperTest < ActionView::TestCase
  test 'full title helper' do
    assert_equal full_title, 'Ranking-Info'
    assert_equal full_title('About'), 'About | Ranking-Info'
  end

  test 'get available quarters' do
    assert_equal(2, fetch_available_quarters.size)
    assert_equal(2, fetch_available_quarters['2017'].size)
    assert_equal('01.07.', fetch_available_quarters['2018'].fetch(0))
  end
end

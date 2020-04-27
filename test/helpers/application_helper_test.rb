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

  test 'get federation long name' do
    assert_equal('Baden', federation_long_name('BAD'))
    assert_equal('Berlin-Brandenburg', federation_long_name('BB'))
    assert_equal('Bayern', federation_long_name('BTV'))
    assert_equal('Hamburg', federation_long_name('HAM'))
    assert_equal('Hessen', federation_long_name('HTV'))
    assert_equal('Rheinland-Pfalz', federation_long_name('RPF'))
    assert_equal('Schleswig-Holstein', federation_long_name('SLH'))
    assert_equal('Saarland', federation_long_name('STB'))
    assert_equal('Sachsen', federation_long_name('STV'))
    assert_equal('Mecklenburg-Vorpommern', federation_long_name('TMV'))
    assert_equal('Niedersachsen-Bremen', federation_long_name('TNB'))
    assert_equal('Sachsen-Anhalt', federation_long_name('TSA'))
    assert_equal('Thüringen', federation_long_name('TTV'))
    assert_equal('Mittelrhein', federation_long_name('TVM'))
    assert_equal('Niederrhein', federation_long_name('TVN'))
    assert_equal('Würtemberg', federation_long_name('WTB'))
    assert_equal('Westfalen', federation_long_name('WTV'))
    assert_equal('HTI', federation_long_name('HTI'))
  end
end

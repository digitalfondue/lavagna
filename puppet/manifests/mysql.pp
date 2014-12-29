class { 'apt':
	always_apt_update    => true
}
->
class { 'mysql::server':
	override_options => { 'mysqld' => { 'bind-address' => '0.0.0.0' } }
}
->
class { 'mysql::client': }
->
mysql_database { $db:
	ensure  => 'present',
	charset => 'utf8',
	collate => 'utf8_bin',
}
->
mysql_user { 'root@%':
	ensure                   => 'present',
	max_connections_per_hour => '0',
	max_queries_per_hour     => '0',
	max_updates_per_hour     => '0',
	max_user_connections     => '0',
}
->
mysql_grant { 'root@%/*.*':
	ensure     => 'present',
	options    => ['GRANT'],
	privileges => ['ALL'],
	table      => '*.*',
	user       => 'root@%',
}
->
exec { 'restart_mysql':
	command => '/usr/sbin/service mysql restart'
}
class {'postgresql::globals':
	manage_package_repo => true,
	encoding => 'UTF8',
	locale   => 'en_US.utf8'
}->
class {'postgresql::server':
	listen_addresses => '*',
	ipv4acls => ['host all all 0.0.0.0/0 md5'],
	ip_mask_deny_postgres_user => '0.0.0.0/32',
	ip_mask_allow_all_users => '0.0.0.0/0'
}

postgresql::server::role { 'postgres':
	password_hash => postgresql_password('postgres', 'password'),
	superuser => true,
	login => true,
	createdb  => true
}

postgresql::server::db { $db:
	user => 'postgres',
	password => postgresql_password('postgres', 'password'),
	encoding => 'UTF8',
	locale   => 'en_US.utf8'
}

class { 'postgresql::server::contrib':
	package_ensure => 'present',
}

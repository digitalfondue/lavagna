VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

	config.vm.define 'pgsql' do |pgsql|
		pgsql.vm.box = 'precise64'
		pgsql.vm.box_url = "http://files.vagrantup.com/precise64.box"

		pgsql.vm.network 'forwarded_port', guest: 5432, host: 5432

		pgsql.vm.provision 'puppet' do |puppet|
			puppet.module_path = 'puppet/modules'
			puppet.manifests_path = 'puppet/manifests'
			puppet.manifest_file = 'pgsql.pp'
			puppet.facter = {
					'db' => 'lavagna'
			}
		end

	end
	
	config.vm.define 'mysql' do |mysql|
		mysql.vm.box = 'precise64'
		mysql.vm.box_url = "http://files.vagrantup.com/precise64.box"

		mysql.vm.network "forwarded_port", guest: 3306, host: 3306

		mysql.vm.provision "puppet" do |puppet|
			puppet.module_path = 'puppet/modules'
			puppet.manifests_path = 'puppet/manifests'
			puppet.manifest_file = 'mysql.pp'
			puppet.facter = {
					'db' => 'lavagna'
			}
		end

	end

	config.vm.define 'oracle' do |oracle|
		oracle.vm.box = 'precise64'
		oracle.vm.box_url = "http://files.vagrantup.com/precise64.box"
		oracle.vm.hostname = "oracle"
		
		oracle.vm.network :forwarded_port, guest: 1521, host: 1521

		oracle.vm.provider :virtualbox do |vb|
			vb.customize ["modifyvm", :id,
										"--name", "oracle",
										"--memory", "512",
										"--natdnshostresolver1", "on"]
		end

		oracle.vm.provision :shell, :inline => "echo \"America/New_York\" | sudo tee /etc/timezone && dpkg-reconfigure --frontend noninteractive tzdata"
		
		oracle.vm.provision :puppet do |puppet|
			puppet.manifests_path = "puppet/manifests"
			puppet.module_path = "puppet/modules"
			puppet.manifest_file = "oracle.pp"
			puppet.options = "--verbose --trace"
		end

	end
end
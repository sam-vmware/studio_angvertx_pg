container.logger.info "Main main.App Starting"
//container.logger.info "Deploying common ..."
//container.deployModule('com.vmware~mods-common~1.0')
container.logger.info "Deploying vami-services ..."
container.deployModule('com.vmware~vami-system-service~1.0')
container.logger.info "Deploying web-server ..."
container.deployModule('com.vmware~vami-web-server~1.0')

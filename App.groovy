container.logger.info "BootStrap Starting"
container.logger.info "Deploying content resolver ..."
container.deployModule('com.vmware~vami-content-resolver~1.0')
container.logger.info "Deploying vami-services ..."
container.deployModule('com.vmware~vami-system-service~1.0')
container.logger.info "Deploying web-server ..."
container.deployModule('com.vmware~vami-web-server~1.0')

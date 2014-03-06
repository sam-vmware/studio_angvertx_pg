container.logger.info "Deploying web-server ..."
container.deployModule('com.vmware~vami-web-server~1.0')
container.logger.info "Deploying content resolver ..."
container.deployModule('com.vmware~vami-content-resolver~1.0')

/*container.logger.info "Deploying vami-services ..."
for (svc in ['com.vmware~vami-system-service~1.0']) {
    container.logger.info "Deploying service $svc ..."
    container.deployModule('com.vmware~vami-system-service~1.0')
}*/

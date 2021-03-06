package com.canarylogic.sing

import org.apache.commons.lang.builder.HashCodeBuilder

/*
OppCategory e.g Consulting,Training,SoftwareDev etc.
 */
class OppCategory  extends AbstractCanaryDomain implements  Serializable{
    def static XML_ELEMENT_MAP = [name:"name"]

    static belongsTo = [client:Client]
    String name


    static constraints = {
        name(blank: false,unique: 'client')
    }

    def beforeDelete() {
        Opportunity.withNewSession {
            Opportunity.remoteAllWithOppCategory(this)
        }
    }

    @Override
    def toXml(def builder){
        builder."$SingUtils.OPP_CATEGORY_ROOT"(){
            id(type:SingUtils.INTEGER_TYPE, id)
            name(name)
        }
    }

    static void saveBean(String xmlRootName,def aMap,def pBean, def client, boolean isUpdateCall){
        if(!isUpdateCall) pBean.client = client
        pBean.save(client:client).save(failOnError:true)
    }

    @Override
    boolean equals(other){
         if(! (other instanceof OppCategory )){
             return false
         }
         other?.client == this.client  && other?.name == this.name
    }

    @Override
    public int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append(client).append(name)
        builder.toHashCode()
    }

    @Override
    String toString(){
        return "$name - $client"
    }


}

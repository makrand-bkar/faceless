package com.canarylogic.sing

import org.apache.commons.lang.builder.HashCodeBuilder

//TBD : Can be optimized by using osome one to Many Techniques for https://github.com/bjornerik/sandbox/
//https://mrpaulwoods.wordpress.com/2011/02/07/implementing-burt-beckwiths-gorm-performance-no-collections/

//hashcode and equals method need to be implemented to take the advantage of Hibernate Second level cache
class Person extends AbstractCanaryDomain implements Serializable{

    def static XML_ELEMENT_MAP = [firstName:"firstName",lastName:"lastName", suffix:"suffix",
                  count:"count", address_list:"contactAddressList",contact_data_list:"contactDetailsList",
                  tag_list:"tagList",notes_list:"notesList"
                  ]  //"id:id

	static hasMany = [contactAddressList:ContactAddress,contactDetailsList:ContactDetails,
            taskList:Tasks,customFieldList:CustomFields]

	Client client
    Company company
	
	String firstName
	String lastName

    String createdBy
    String updatedBy



    def getOppMemberList(){
       OppMember.findAllByPerson(this)
    }

    def getNotesList(){
        Notes.findAllByPerson(this)
    }

    Set<Tag> getTagList(){
        PersonTag.findAllByPerson(this).collect { it.tag } as Set
    }

    boolean hasTag(Tag tag) {
        PersonTag.countByPersonAndTag(this, tag) > 0
    }

    def beforeDelete() {
        OppMember.withNewSession(){
            oppMemberList*.delete()
        }
        Notes.withNewSession {
            notesList*.delete()
        }
        PersonTag.withNewSession {
            PersonTag.removeAllWithPerson(this)
        }

    }

	static constraints = {
        firstName(nullable:false,blank:false, unique:['lastName','client'])
		lastName(blank:false)
        company(nullable:true)
        createdBy(editable:false)
        updatedBy(nullable:true)
	}



    @Override
    def toXml(def builder,boolean isListView=true){
      def mkp = builder.getMkp()
      builder.person(){
          id(id)
          mkp.comment("required")
          firstName(firstName)
          mkp.comment("required")
          lastName(lastName)
          address_list(){
              contactAddressList.each { aAddress ->
                  aAddress.toXml(builder)
              }
          }
          contact_data_list(){
              contactDetailsList.each { cdetails ->
                  cdetails.toXml(builder)
              }
          }
          if(!isListView){
              tag_list(){
                  tagList.each{ aTag ->
                      aTag.toXml(builder)
                  }
              }
              notes_list(){
                  notesList.each{ aNote ->
                     aNote.toXml(builder)
                  }
              }
          }
          date_created(type:Constants.DATETIME_TYPE,dateCreated)
          last_updated(type:Constants.DATETIME_TYPE,lastUpdated)
          createdBy(createdBy)
          updatedBy(updatedBy)
      }
    }

    static void saveBean(String xmlRootName,def aMap,def pBean, def client, boolean isUpdateCall){
        if(!pBean.client)
             pBean.client = client
        if(aMap.contactAddressList){
            pBean.contactAddressList.each{
                if(!it.person) it.person = pBean
            }
        }

        if(aMap.contactDetailsList){
            pBean.contactDetailsList.each{
                if(!it.person) it.person = pBean
            }
        }

        pBean.save(failOnError:true,flush:true)
        pBean.errors.each{
            println it
        }

    }


    @Override
    boolean equals(other){
         if(! (other instanceof Person )){
             return false
         }
         other?.firstName == this.firstName &&
                 other?.lastName == this.lastName  &&
                    other?.client == this.client
    }

    @Override
    int hashCode(){
        def builder = new HashCodeBuilder()
        builder.append(firstName).append(lastName).append(client)
        builder.toHashCode()
    }

    @Override
    String toString(){
        "$firstName $lastName for client $client"
    }






    
}

/*****************************************************
 *      THIS IS A GENERATED FILE. DO NOT EDIT.  SINTEFBOARD_MAIN_HEADER.H
 *      Implementation for Application /*NAME*/
 *  Generated from ThingML (http://www.thingml.org)
 *****************************************************/

typedef unsigned char byte; 
/*TYPEDEFS*/

/*C_HEADERS*/

/*RUNTIME_CLASS*/

class /*NAME*/ : public ThingMlRuntime_class {
private:
#ifdef RCDPORT_IN_USE
port_class *Ports_ptr;
#endif
/*HEADER_CLASS*/

//CTX begin
/*HEADER_CONTEXT*/
//CTX end
//Conf begin
/*HEADER_CONFIGURATION*/
//Conf end

public:
#ifdef RCDPORT_IN_USE
void setup(port_class *ports_ptr);
#else
void setup(void);
#endif
void loop(void);
#ifdef RCDPORT_IN_USE
void receive_forward(msgc_t *msg_in_ptr, int16_t from_port);
#endif

};





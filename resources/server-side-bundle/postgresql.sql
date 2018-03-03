-- DONT UPLOAD THIS FILE TO YOUR WEB SERVER. 
-- Its for the jdbwctest package if you want to test the driver.
-- Its for inserting into the web servers database only.
-- Otherwise you can ignore this file.

--
-- Name: test01; Type: TABLE; Schema: public;
--
CREATE TABLE test01 (
    valkey character varying(32) NOT NULL,
    expiry bigint DEFAULT (0)::bigint NOT NULL,
    value text,
    myidx integer NOT NULL
);


--
-- Name: test02; Type: TABLE; Schema: public;
--
CREATE TABLE test02 (
    valkey character varying(32) NOT NULL,
    accessed timestamp without time zone NOT NULL,
    test02_id integer NOT NULL
);


CREATE SEQUENCE test01_myidx_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

--
-- Name: test01_myidx_seq; Type: SEQUENCE OWNED BY; Schema: public;
--
ALTER SEQUENCE test01_myidx_seq OWNED BY test01.myidx;


--
-- Name: test02_test02_id_seq; Type: SEQUENCE; Schema: public;
--
CREATE SEQUENCE test02_test02_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

--
-- Name: test02_test02_id_seq; Type: SEQUENCE OWNED BY; Schema: public;
--
ALTER SEQUENCE test02_test02_id_seq OWNED BY test02.test02_id;


--
-- Name: myidx; Type: DEFAULT; Schema: public;
--
ALTER TABLE test01 ALTER COLUMN myidx SET DEFAULT nextval('test01_myidx_seq'::regclass);


--
-- Name: test02_id; Type: DEFAULT; Schema: public;
--
ALTER TABLE test02 ALTER COLUMN test02_id SET DEFAULT nextval('test02_test02_id_seq'::regclass);


--
-- Name: test01_pkey; Type: CONSTRAINT; Schema: public;
--
ALTER TABLE ONLY test01 ADD CONSTRAINT test01_pkey PRIMARY KEY (myidx);


--
-- Name: test02_pkey; Type: CONSTRAINT; Schema: public;
--
ALTER TABLE ONLY test02 ADD CONSTRAINT test02_pkey PRIMARY KEY (test02_id);


--
-- Name: valkey; Type: CONSTRAINT; Schema: public;
--
ALTER TABLE ONLY test01 ADD CONSTRAINT valkey UNIQUE (valkey);


--
-- Name: expiry1; Type: INDEX; Schema: public;
--
CREATE INDEX expiry1 ON test01 USING btree (expiry);

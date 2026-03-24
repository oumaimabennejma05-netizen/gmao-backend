--
-- PostgreSQL database dump
--

\restrict AzD15abBh0htDkEoopO6EohD7u5KCVLEsI1YAlKx7YonpLTOhptdYmeQJWxg3tS

-- Dumped from database version 15.17 (Ubuntu 15.17-1.pgdg24.04+1)
-- Dumped by pg_dump version 15.17 (Ubuntu 15.17-1.pgdg24.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: machine_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.machine_data (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    runtime double precision,
    temperature double precision,
    vibration double precision,
    machine_id bigint NOT NULL
);


--
-- Name: machine_data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.machine_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: machine_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.machine_data_id_seq OWNED BY public.machine_data.id;


--
-- Name: machines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.machines (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    location character varying(255),
    maintenance_date date,
    model character varying(255),
    name character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT machines_status_check CHECK (((status)::text = ANY ((ARRAY['OPERATIONAL'::character varying, 'MAINTENANCE'::character varying, 'BROKEN'::character varying, 'IDLE'::character varying])::text[])))
);


--
-- Name: machines_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.machines_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: machines_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.machines_id_seq OWNED BY public.machines.id;


--
-- Name: task_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_history (
    id bigint NOT NULL,
    completed_at timestamp(6) without time zone,
    notes text,
    task_id bigint NOT NULL,
    technician_id bigint
);


--
-- Name: task_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_history_id_seq OWNED BY public.task_history.id;


--
-- Name: tasks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tasks (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    due_date date,
    priority character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    machine_id bigint,
    technician_id bigint,
    CONSTRAINT tasks_priority_check CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying, 'CRITICAL'::character varying])::text[]))),
    CONSTRAINT tasks_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'PENDING_APPROVAL'::character varying, 'COMPLETED'::character varying, 'APPROVED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tasks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    address character varying(255),
    created_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    id_number character varying(255),
    name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    profile_picture character varying(255),
    role character varying(255) NOT NULL,
    two_factor_enabled boolean NOT NULL,
    two_factor_secret character varying(255),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'RESPONSABLE'::character varying, 'TECHNICIAN'::character varying])::text[])))
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: machine_data id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.machine_data ALTER COLUMN id SET DEFAULT nextval('public.machine_data_id_seq'::regclass);


--
-- Name: machines id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.machines ALTER COLUMN id SET DEFAULT nextval('public.machines_id_seq'::regclass);


--
-- Name: task_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_history ALTER COLUMN id SET DEFAULT nextval('public.task_history_id_seq'::regclass);


--
-- Name: tasks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: machine_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.machine_data (id, created_at, runtime, temperature, vibration, machine_id) FROM stdin;
1	2026-03-23 20:01:25.314503	950	87.5	3.2	1
2	2026-03-23 20:01:25.318566	948	85	3	1
3	2026-03-23 20:01:25.321028	320	55	2.1	2
4	2026-03-23 20:01:25.323958	318	53	2	2
5	2026-03-23 20:01:25.326595	1100	65	8.9	3
6	2026-03-23 20:01:25.328692	1098	68	9.1	3
7	2026-03-23 20:01:25.33105	820	72	4.8	4
8	2026-03-23 20:01:25.333125	1350	95	7.5	5
\.


--
-- Data for Name: machines; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.machines (id, created_at, location, maintenance_date, model, name, status) FROM stdin;
1	2026-03-23 20:01:25.308196	Workshop A	2026-05-23	FANUC-30i	CNC Machine Alpha	OPERATIONAL
2	2026-03-23 20:01:25.309486	Workshop B	2026-04-23	DMG-CLX500	Lathe Machine Beta	OPERATIONAL
3	2026-03-23 20:01:25.310324	Press Room	2026-03-18	ENERPAC-P142	Hydraulic Press Gamma	MAINTENANCE
4	2026-03-23 20:01:25.31106	Utility Room	2026-06-23	ATLAS-GA55	Air Compressor Delta	OPERATIONAL
5	2026-03-23 20:01:25.311823	Assembly Line	2026-03-23	INTRALOX-T500	Conveyor Belt Epsilon	BROKEN
\.


--
-- Data for Name: task_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.task_history (id, completed_at, notes, task_id, technician_id) FROM stdin;
1	2026-03-23 20:31:53.468382	Task submitted for approval	1	3
2	2026-03-23 20:32:23.522228	Task approved by responsable	1	2
\.


--
-- Data for Name: tasks; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tasks (id, created_at, description, due_date, priority, status, title, machine_id, technician_id) FROM stdin;
2	2026-03-23 20:01:25.340572	Critical vibration detected. Inspect bearings, mounting, and hydraulic seals.	2026-03-23	CRITICAL	PENDING	Hydraulic Press Emergency Maintenance	3	4
4	2026-03-23 20:01:25.34616	Perform monthly lubrication routine on DMG CLX500 lathe machine.	2026-03-20	LOW	COMPLETED	Lathe Monthly Lubrication	2	4
5	2026-03-23 20:01:25.347615	Replace air filters on Atlas GA55 compressor. Runtime approaching maintenance threshold.	2026-03-30	MEDIUM	PENDING	Compressor Filter Replacement	4	3
3	2026-03-23 20:01:25.344843	Conveyor belt has stopped. Diagnose fault and replace damaged components.	2026-03-23	CRITICAL	IN_PROGRESS	Conveyor Belt Repair	5	3
1	2026-03-23 20:01:25.335488	Inspect and clean cooling system on CNC Machine Alpha. Temperature trending above warning threshold.	2026-03-25	HIGH	APPROVED	CNC Temperature Inspection	1	3
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, address, created_at, email, id_number, name, password, profile_picture, role, two_factor_enabled, two_factor_secret) FROM stdin;
1	123 Industrial Zone, Tunis	2026-03-23 20:01:25.277697	admin@gmao.com	ADM001	Admin GMAO	$2a$10$Qe82xiI/LDL3DLmFxrkrG.KHX6LMKmtm86bhqF6apuRf0sCx//VAa	\N	ADMIN	f	\N
2	456 Factory St, Sfax	2026-03-23 20:01:25.300351	responsable@gmao.com	RSP001	Mohamed Ali	$2a$10$HYmqTuzwLdO3.QOOrYSSuOUneBo6dfvc89uVdJMTo4ETHLNoZpKsq	\N	RESPONSABLE	f	\N
3	789 Workshop Ave, Sousse	2026-03-23 20:01:25.301449	tech1@gmao.com	TCH001	Karim Sassi	$2a$10$Ubljmy05tOEKrdKGDVOF/eCfqlEbQa6dAvGZQP2yUlELtyU3W1i3m	\N	TECHNICIAN	f	\N
4	321 Maintenance Rd, Monastir	2026-03-23 20:01:25.302539	tech2@gmao.com	TCH002	Amira Ben Salah	$2a$10$NuDI9iwiQ6IMmAgQsuyxeuATrQVfHzai/BzkvN2Bi4HsBBab9RVsu	\N	TECHNICIAN	f	\N
\.


--
-- Name: machine_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.machine_data_id_seq', 8, true);


--
-- Name: machines_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.machines_id_seq', 5, true);


--
-- Name: task_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.task_history_id_seq', 2, true);


--
-- Name: tasks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tasks_id_seq', 5, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 4, true);


--
-- Name: machine_data machine_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.machine_data
    ADD CONSTRAINT machine_data_pkey PRIMARY KEY (id);


--
-- Name: machines machines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.machines
    ADD CONSTRAINT machines_pkey PRIMARY KEY (id);


--
-- Name: task_history task_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_history
    ADD CONSTRAINT task_history_pkey PRIMARY KEY (id);


--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: tasks fka01fey26ut89aod3trb9xgris; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT fka01fey26ut89aod3trb9xgris FOREIGN KEY (technician_id) REFERENCES public.users(id);


--
-- Name: machine_data fkdix41a7pihf42htbejorsifx0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.machine_data
    ADD CONSTRAINT fkdix41a7pihf42htbejorsifx0 FOREIGN KEY (machine_id) REFERENCES public.machines(id);


--
-- Name: task_history fke6pig0w111eiymhut3w14r485; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_history
    ADD CONSTRAINT fke6pig0w111eiymhut3w14r485 FOREIGN KEY (technician_id) REFERENCES public.users(id);


--
-- Name: tasks fkgyg8fwxqojakg6ijnc4jsgktt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT fkgyg8fwxqojakg6ijnc4jsgktt FOREIGN KEY (machine_id) REFERENCES public.machines(id);


--
-- Name: task_history fkjqraeud129avhcva579fhioj3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_history
    ADD CONSTRAINT fkjqraeud129avhcva579fhioj3 FOREIGN KEY (task_id) REFERENCES public.tasks(id);


--
-- PostgreSQL database dump complete
--

\unrestrict AzD15abBh0htDkEoopO6EohD7u5KCVLEsI1YAlKx7YonpLTOhptdYmeQJWxg3tS


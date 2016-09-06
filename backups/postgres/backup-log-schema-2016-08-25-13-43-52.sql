--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.1
-- Dumped by pg_dump version 9.5.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: log; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA log;


ALTER SCHEMA log OWNER TO postgres;

--
-- Name: SCHEMA log; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA log IS 'A Lavinia-naplók tartalma: mérősezközök, események, felhasználók
2015. június';


SET search_path = log, pg_catalog;

--
-- Name: illness_record_type; Type: TYPE; Schema: log; Owner: postgres
--

CREATE TYPE illness_record_type AS (
	illness_id integer,
	parameter character varying,
	param_label_id integer
);


ALTER TYPE illness_record_type OWNER TO postgres;

--
-- Name: rda_record_type; Type: TYPE; Schema: log; Owner: postgres
--

CREATE TYPE rda_record_type AS (
	nutr_id integer,
	min_value real,
	opt_value real,
	max_value real,
	unit_id integer,
	unit_label character varying
);


ALTER TYPE rda_record_type OWNER TO postgres;

--
-- Name: anamnesis_record_type; Type: TYPE; Schema: log; Owner: postgres
--

CREATE TYPE anamnesis_record_type AS (
	event_id integer,
	ts_specified timestamp without time zone,
	ts_recorded timestamp without time zone,
	height integer,
	weight real,
	birth_date date,
	gender_code smallint,
	lifestyle_code smallint,
	sport_code smallint,
	mass_change real,
	mass_change_time integer,
	egfr real,
	steorid_treatment boolean,
	insulin_dose real,
	illness_list illness_record_type[],
	rda_list rda_record_type[]
);


ALTER TYPE anamnesis_record_type OWNER TO postgres;

--
-- Name: illness_nutrient_record_type; Type: TYPE; Schema: log; Owner: postgres
--

CREATE TYPE illness_nutrient_record_type AS (
	nutr_id integer,
	nutr_name character varying,
	unit_id integer,
	unit_name character varying
);


ALTER TYPE illness_nutrient_record_type OWNER TO postgres;

--
-- Name: illness_full_record_type; Type: TYPE; Schema: log; Owner: postgres
--

CREATE TYPE illness_full_record_type AS (
	illness_id integer,
	illness_name character varying,
	illness_nutrient_list illness_nutrient_record_type[],
	illness_param_list illness_record_type[]
);


ALTER TYPE illness_full_record_type OWNER TO postgres;

--
-- Name: illness_list; Type: TYPE; Schema: log; Owner: postgres
--

CREATE TYPE illness_list AS (
	illness_id integer,
	parameter character varying
);


ALTER TYPE illness_list OWNER TO postgres;

--
-- Name: add_anamnesis(integer, timestamp without time zone, timestamp without time zone, integer, real, date, smallint, smallint, smallint, real, integer, real, boolean, real, illness_record_type[], rda_record_type[]); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION add_anamnesis(var_episode_id integer, var_ts_specified timestamp without time zone, var_ts_recorded timestamp without time zone, var_height integer, var_weight real, var_birth_date date, var_gender_code smallint, var_lifestyle_code smallint, var_sport_code smallint, var_mass_change real, var_mass_change_time integer, var_egfr real, var_steorid_treatment boolean, var_insulin_dose real, var_illness_list illness_record_type[], var_rda_list rda_record_type[]) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
declare var_event_id integer;
declare var_rda_record log.rda_record_type;
declare var_illness_record log.illness_record_type;
begin
	var_event_id := nextval('log.ep_event_event_id_seq'::regclass);
	insert into log.ep_event (event_id, episode_id, event_type_code, status_code,
		ts_specified, ts_recorded, ts_received, source_device_id)
	values	(var_event_id, var_episode_id, 
		1,  --anamnézis
		1, 	--normál bejegyzés
		var_ts_specified, var_ts_recorded, now(), 
		1	--a kézi bevitel device id-je
	);
	insert into log.event_anamnesis (event_id, height ,  weight, birth_date , gender_code ,
		lifestyle_code ,  sport_code ,  mass_change , mass_change_time, egfr, steorid_treatment, insulin_dose)
	values (var_event_id, var_height ,  var_weight, var_birth_date , var_gender_code ,
		var_lifestyle_code ,  var_sport_code ,  var_mass_change , var_mass_change_time, 
		var_egfr, var_steorid_treatment, var_insulin_dose);
	--a kapott betegség-paraméterek mentése
	foreach var_illness_record in array var_illness_list loop
		insert into log.event_anamnesis_illness (anamnesis_id, illness_id, parameter, param_label_id)
		values (var_event_id, var_illness_record.illness_id, var_illness_record.parameter, var_illness_record.param_label_id);
	end loop;
	--a kapott rda-paraméterek mentése
	foreach var_rda_record in array var_rda_list loop
		insert into log.event_anamnesis_rda (anamnesis_id, nutr_id, min_value, opt_value, max_value, unit_id, unit_label)
		values (var_event_id, var_rda_record.nutr_id, var_rda_record.min_value, var_rda_record.opt_value, 
				var_rda_record.max_value, var_rda_record.unit_id, var_rda_record.unit_label);
	end loop;
	return var_event_id;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;
$$;


ALTER FUNCTION log.add_anamnesis(var_episode_id integer, var_ts_specified timestamp without time zone, var_ts_recorded timestamp without time zone, var_height integer, var_weight real, var_birth_date date, var_gender_code smallint, var_lifestyle_code smallint, var_sport_code smallint, var_mass_change real, var_mass_change_time integer, var_egfr real, var_steorid_treatment boolean, var_insulin_dose real, var_illness_list illness_record_type[], var_rda_list rda_record_type[]) OWNER TO postgres;

--
-- Name: FUNCTION add_anamnesis(var_episode_id integer, var_ts_specified timestamp without time zone, var_ts_recorded timestamp without time zone, var_height integer, var_weight real, var_birth_date date, var_gender_code smallint, var_lifestyle_code smallint, var_sport_code smallint, var_mass_change real, var_mass_change_time integer, var_egfr real, var_steorid_treatment boolean, var_insulin_dose real, var_illness_list illness_record_type[], var_rda_list rda_record_type[]); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION add_anamnesis(var_episode_id integer, var_ts_specified timestamp without time zone, var_ts_recorded timestamp without time zone, var_height integer, var_weight real, var_birth_date date, var_gender_code smallint, var_lifestyle_code smallint, var_sport_code smallint, var_mass_change real, var_mass_change_time integer, var_egfr real, var_steorid_treatment boolean, var_insulin_dose real, var_illness_list illness_record_type[], var_rda_list rda_record_type[]) IS '
új anamnézis felvétele létező epizódhoz
Futtatás: 
select log.add_anamnesis(
1, --ep.id
''2015-07-26 17:10:14'', --specified
''2015-07-26 18:10:14'', --recorded
175, --height
72.9, --weight
''1972-03-26'', --birth
1::smallint, --férfi
1::smallint, --irodai munka
1::smallint, --nincs sport
1.5, --mass_change
2, --2 hónap alatt
null, --egfr
null, --steroid
null, --ins.dose
array [ (72, ''Kalciumoxalát kő'', 41185)::log.illness_record_type, 		--vesekövesség 
	(74, ''Inzulinnal kezelt'', 41182)::log.illness_record_type			--II típ. cukorb.
],	 
array [	(8, 1447, 1459, 1690, 9, ''kcal'')::log.rda_record_type,	--energia
	(5, 45, 50, 55.2, 5, ''g'')::log.rda_record_type,		--össz. zsír
	(28, 1500, 1750, 2000.5, 14, ''mg'')::log.rda_record_type	--nátrium
]	--rekord mezői: nutr_id integer, min_value real, opt_value real, max_value real, unit_id integer, unit_label varchar
);

A ::smallint azért kell, mert különben integernek veszi a paramétert, és nem találja meg a függvényt
';


--
-- Name: add_group_member(integer, integer, date, date, character varying, character varying); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
begin
	insert into log.group_member (
		user_id, group_id , date_start, date_end, internal_group_id, external_group_id )
	values (var_user_id, var_group_id, var_date_start, var_date_end, var_internal_group_id, var_external_group_id);
	return 1;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;
$$;


ALTER FUNCTION log.add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) OWNER TO postgres;

--
-- Name: FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) IS '
felhasználó hozzáadása létező csoporthoz, pl. fogyópárhoz a log sémában
Futtatás pl egy fogyópár létrehozása: 
select log.add_group_member(11,1,''2015-07-26'',null, null, null);
select log.add_group_member(14,1,''2015-07-26'',null, null, null);
';


--
-- Name: add_user(smallint, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, integer, character varying, smallint, character varying); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying DEFAULT NULL::character varying) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
declare var_user_id integer;
begin
	var_user_id := nextval('log.user_user_id_seq'::regclass);
	insert into log.user (
		user_id , user_type_code ,   firstname ,  family_name  , mobile ,   skype ,  email ,   google_account ,   ios_account ,   ds_id ,   
		lavinia_name , 	default_insulin_type_id ,   user_desc ,   illness_type_code  )
	values (var_user_id, var_user_type_code ,   var_firstname ,  var_family_name , var_mobile ,   var_skype ,  var_email ,   var_google_account ,  
		var_ios_account ,   var_ds_id ,   var_lavinia_name , 	var_default_insulin_type_id ,   var_user_desc ,   var_illness_type_code);
	if var_user_type_code=2 then
		insert into log.expert_user (user_id, organisation) values (var_user_id, var_expert_organisation);
	end if;
	return var_user_id;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;

$$;


ALTER FUNCTION log.add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying) OWNER TO postgres;

--
-- Name: FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying) IS '
Új felhasználó felvétele a log sémába
Futtatás: select log.add_user(1::smallint,  null, null, null, null, null, null, null, null, null, null, null, 1::smallint ); 
1: tesztelő felh. típus 1: egészséges felh. típus
a var_expert_organisation opcionális paraméter, ha az user_zype_code=2, akkor számít csak
orvos vagy dietetikus esetén:
select log.add_user(2::smallint,  ''Teszt'', ''Orvos'', null, null, null, null, null, null, null, null, ''diabetológus orvos'', 1::smallint, ''Vanderlich Egészségcentrum'' ); 
A ::smallint azért kell, mert különben integernek veszi a paramétert, és nem találja meg a függvényt
';


--
-- Name: add_user_group(smallint, character varying, integer); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
declare var_group_id integer;
begin
	var_group_id := nextval('log.user_group_group_id_seq'::regclass);
	insert into log.user_group (
		group_id , group_type_code ,  group_name ,  group_create_date,  expert_id)
	values (var_group_id, var_group_type_code ,   var_groupname , now(),  var_expert_id);
	return var_group_id;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;
$$;


ALTER FUNCTION log.add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) OWNER TO postgres;

--
-- Name: FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) IS '
Új felhasználó-csoport felvétele a log sémába
Futtatás: select select log.add_user_group(1::smallint, ''teszt fogyópár'', 12);
A ::smallint azért kell, mert különben integernek veszi a paramétert, és nem találja meg a függvényt
';


--
-- Name: get_last_anamnesis(integer); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION get_last_anamnesis(var_episode_id integer) RETURNS anamnesis_record_type
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
declare var_event_id integer;
declare var_anamnesis_record log.anamnesis_record_type;
declare var_illness_record log.illness_record_type;
declare var_rda_record log.rda_record_type;
declare var_illness_list log.illness_record_type[]  := '{}';
declare var_rda_list log.rda_record_type[] := '{}';
declare var_i integer;

begin
	var_anamnesis_record.event_id := null;
	--a legutóbbi anamnézis rekord ebben az epizódban
	select e.event_id, ts_specified, ts_recorded, height, weight, birth_date, gender_code, lifestyle_code, sport_code,
		mass_change, mass_change_time, egfr, steorid_treatment, insulin_dose into
		var_anamnesis_record.event_id, var_anamnesis_record.ts_specified, var_anamnesis_record.ts_recorded, 
		var_anamnesis_record.height, var_anamnesis_record.weight, var_anamnesis_record.birth_date, 
		var_anamnesis_record.gender_code, var_anamnesis_record.lifestyle_code, 
		var_anamnesis_record.sport_code, var_anamnesis_record.mass_change, var_anamnesis_record.mass_change_time, 
		var_anamnesis_record.egfr, var_anamnesis_record.steorid_treatment, var_anamnesis_record.insulin_dose 
	from log.ep_event e inner join log.event_anamnesis a on e.event_id=a.event_id
	where e.episode_id=var_episode_id and e.event_type_code=1 order by ts_recorded desc limit 1;
	--raise notice 'eventid: %', var_anamnesis_record.event_id ;
	--kapcsolódó rda rekordok
	if var_anamnesis_record.event_id is not null then
		var_i := 1;
		for var_rda_record in select nutr_id , min_value , opt_value , 	max_value , unit_id , unit_label
			from log.event_anamnesis_rda where anamnesis_id=var_anamnesis_record.event_id
		loop
--			array_append(var_rda_list, var_rda_record);  --nem működik
			var_rda_list[var_i] := var_rda_record;
			var_i := var_i + 1;
		end loop;
		var_i := 1;
		for var_illness_record in select illness_id, parameter, param_label_id
			from log.event_anamnesis_illness where anamnesis_id=var_anamnesis_record.event_id
		loop
--			array_append(var_illness_list, var_illness_record);
			var_illness_list[var_i] := var_illness_record;
			var_i := var_i + 1;
		end loop;
		var_anamnesis_record.illness_list := var_illness_list;
		var_anamnesis_record.rda_list := var_rda_list;
		return var_anamnesis_record;
	else return null;
	end if;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;
$$;


ALTER FUNCTION log.get_last_anamnesis(var_episode_id integer) OWNER TO postgres;

--
-- Name: FUNCTION get_last_anamnesis(var_episode_id integer); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION get_last_anamnesis(var_episode_id integer) IS '
a legutóbbi anamnézis lekérdezése
Futtatás: 
select * from log.get_last_anamnesis(1);
';


--
-- Name: lb_insert_device(character varying, integer, smallint); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION lb_insert_device(var_hw_serial character varying, var_model_id integer, var_dev_type_code smallint) RETURNS integer
    LANGUAGE plpgsql
    AS $$

declare var_next_id integer;
	begin
		var_next_id := nextval('log.device_device_id_seq'::regclass);
		insert into log.device(device_id, hw_serial, model_id, dev_type_code) values(var_next_id, var_hw_serial, var_model_id, var_dev_type_code);
		return var_next_id;
	end;
$$;


ALTER FUNCTION log.lb_insert_device(var_hw_serial character varying, var_model_id integer, var_dev_type_code smallint) OWNER TO postgres;

--
-- Name: lb_insert_episode(integer, timestamp without time zone, smallint); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION lb_insert_episode(arg_user_id integer, arg_start_date timestamp without time zone, arg_ep_type_code smallint) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare var_episode_id integer;
begin
	var_episode_id := nextval('log.episode_episode_id_seq'::regclass);
	insert into log.episode(episode_id, user_id, start_date, end_date, ep_type_code) 
		values(var_episode_id, arg_user_id, arg_start_date, null, arg_ep_type_code);

	return var_episode_id;
end;
$$;


ALTER FUNCTION log.lb_insert_episode(arg_user_id integer, arg_start_date timestamp without time zone, arg_ep_type_code smallint) OWNER TO postgres;

--
-- Name: lb_insert_episode_device(integer, integer, smallint); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION lb_insert_episode_device(arg_user_id integer, arg_device_id integer, arg_device_type smallint) RETURNS void
    LANGUAGE plpgsql
    AS $$
begin
	insert into log.episode_device(episode_id, device_id, device_type_code) values(arg_user_id, arg_device_id, arg_device_type);
end;
$$;


ALTER FUNCTION log.lb_insert_episode_device(arg_user_id integer, arg_device_id integer, arg_device_type smallint) OWNER TO postgres;

--
-- Name: lb_insert_user(smallint, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION lb_insert_user(arg_user_type_code smallint, arg_firstname character varying, arg_family_name character varying, arg_email character varying, arg_password character varying, arg_ds_id character varying, arg_user_desc character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare var_next_id integer;
begin
	var_next_id := nextval('log.user_user_id_seq'::regclass);
	insert into log.user(user_id, user_type_code, firstname, family_name, email, google_account, ds_id, user_desc)
		values (var_next_id, arg_user_type_code, arg_firstname, arg_family_name, arg_email, arg_email, arg_ds_id, arg_user_desc);

	if arg_user_type_code = 2 then
		insert into log.expert_user(user_id, organisation) values(var_next_id, 'dummy szöveg egyenlőre, lsd: plpgsql script');
	end if;

	return var_next_id;
end;
$$;


ALTER FUNCTION log.lb_insert_user(arg_user_type_code smallint, arg_firstname character varying, arg_family_name character varying, arg_email character varying, arg_password character varying, arg_ds_id character varying, arg_user_desc character varying) OWNER TO postgres;

--
-- Name: list_code_dict(integer); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION list_code_dict(var_type_id integer DEFAULT NULL::integer) RETURNS TABLE(ctype_id integer, ctype_name character varying, citem_id integer, citem_name character varying)
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
begin
	if var_type_id is null then return query select type_id, type_name, item_id, item_name from log.code_dict order by type_id, item_id;
	else return query select type_id, type_name, item_id, item_name from log.code_dict where type_id=var_type_id order by type_id, item_id;
	end if;
	return;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return;
end;
$$;


ALTER FUNCTION log.list_code_dict(var_type_id integer) OWNER TO postgres;

--
-- Name: FUNCTION list_code_dict(var_type_id integer); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION list_code_dict(var_type_id integer) IS '
a kódszótár lekérdezése, a kódkészlet id paraméter opcionális
Futtatás: 
select * from log.list_code_dict(); --a teljes kódszótár
select * from log.list_code_dict(2); --a 2. kódkészlet
';


--
-- Name: start_episode(integer, smallint, date); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date DEFAULT now()) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
declare var_episode_id integer;
begin
	var_episode_id := nextval('log.episode_episode_id_seq'::regclass);
	insert into log.episode (episode_id, user_id, start_date, ep_type_code)
	values	(var_episode_id, var_user_id, var_start_date, var_ep_type_code );
	return var_episode_id;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;
$$;


ALTER FUNCTION log.start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date) OWNER TO postgres;

--
-- Name: FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date) IS '
új epizód létező felhasználóhoz
Futtatás: 
select log.start_episode(11,2::smallint);
(ingyenes Lavinia felhasználói epizód)
A ::smallint azért kell, mert különben integernek veszi a paramétert, és nem találja meg a függvényt
';


--
-- Name: test(integer, illness_list[]); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION test(var_user_id integer, var_illness_list illness_list[]) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
declare var_episode_id integer;
declare var_i integer;
declare var_illness_record log.illness_list;
begin
	foreach var_illness_record in array var_illness_list loop
		raise notice 'id: %', var_illness_record.illness_id;
	end loop;
	--var_illness_record := var_illness_list[2];
	return var_illness_record.illness_id;
exception when others then 
	raise notice 'SQL warning: % %', SQLERRM, SQLSTATE;
	return null;
end;
$$;


ALTER FUNCTION log.test(var_user_id integer, var_illness_list illness_list[]) OWNER TO postgres;

--
-- Name: truncate_all_tables_in_schema(); Type: FUNCTION; Schema: log; Owner: postgres
--

CREATE FUNCTION truncate_all_tables_in_schema() RETURNS void
    LANGUAGE plpgsql
    AS $$
declare 
	statements cursor for
		select tablename from pg_tables
		where schemaname = 'log';
begin
	for stmt in statements loop
		execute 'TRUNCATE TABLE log.' || quote_ident(stmt.tablename) || ' CASCADE;';
	end loop;

end;
$$;


ALTER FUNCTION log.truncate_all_tables_in_schema() OWNER TO postgres;

--
-- Name: FUNCTION truncate_all_tables_in_schema(); Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON FUNCTION truncate_all_tables_in_schema() IS 'A log sémában található összes táblát kiüríti';


SET default_tablespace = '';

SET default_with_oids = true;

--
-- Name: code_item; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE code_item (
    type_id integer NOT NULL,
    item_id integer NOT NULL,
    item_name character varying
);


ALTER TABLE code_item OWNER TO postgres;

SET default_with_oids = false;

--
-- Name: code_type; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE code_type (
    type_id integer NOT NULL,
    type_name character varying
);


ALTER TABLE code_type OWNER TO postgres;

--
-- Name: code_dict; Type: MATERIALIZED VIEW; Schema: log; Owner: postgres
--

CREATE MATERIALIZED VIEW code_dict AS
 SELECT ct.type_id,
    ct.type_name,
    ci.item_id,
    ci.item_name
   FROM (code_type ct
     JOIN code_item ci ON ((ct.type_id = ci.type_id)))
  WITH NO DATA;


ALTER TABLE code_dict OWNER TO postgres;

--
-- Name: data_type; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE data_type (
    type_id integer NOT NULL,
    type_name_label_id integer NOT NULL,
    default_unit_id integer
);


ALTER TABLE data_type OWNER TO postgres;

--
-- Name: TABLE data_type; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE data_type IS 'adattípus, pl vércukorszint';


--
-- Name: data_type_type_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE data_type_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE data_type_type_id_seq OWNER TO postgres;

--
-- Name: data_type_type_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE data_type_type_id_seq OWNED BY data_type.type_id;


--
-- Name: device; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE device (
    device_id integer NOT NULL,
    hw_serial character varying,
    model_id integer,
    dev_type_code smallint NOT NULL
);


ALTER TABLE device OWNER TO postgres;

--
-- Name: TABLE device; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE device IS 'adatot szolgáltató, eseményt generáló eszköz';


--
-- Name: COLUMN device.hw_serial; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN device.hw_serial IS 'a hw azonosító, ha van';


--
-- Name: COLUMN device.model_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN device.model_id IS 'az eszköz típusa pl. nexus 5';


--
-- Name: COLUMN device.dev_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN device.dev_type_code IS 'kódkészlet #10 pl. "kézi bevitel", "google fit import", "számított érték", "külső mérőeszköz"';


--
-- Name: device_data_type; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE device_data_type (
    model_id integer NOT NULL,
    type_id integer NOT NULL,
    "precision" real,
    unit_id integer
);


ALTER TABLE device_data_type OWNER TO postgres;

--
-- Name: TABLE device_data_type; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE device_data_type IS 'az eszköztípus milyen adattípusokat tud mérni, pl. mellkaspánt HR-t és RR-t';


--
-- Name: device_device_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE device_device_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE device_device_id_seq OWNER TO postgres;

--
-- Name: device_device_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE device_device_id_seq OWNED BY device.device_id;


--
-- Name: device_model; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE device_model (
    model_id integer NOT NULL,
    model_name_label_id integer
);


ALTER TABLE device_model OWNER TO postgres;

--
-- Name: TABLE device_model; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE device_model IS 'pl. Guardian RT CGM';


--
-- Name: device_model_model_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE device_model_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE device_model_model_id_seq OWNER TO postgres;

--
-- Name: device_model_model_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE device_model_model_id_seq OWNED BY device_model.model_id;


--
-- Name: ep_event; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE ep_event (
    event_id integer NOT NULL,
    episode_id integer NOT NULL,
    event_type_code smallint NOT NULL,
    status_code smallint NOT NULL,
    ts_specified timestamp(0) without time zone NOT NULL,
    ts_recorded timestamp without time zone NOT NULL,
    ts_received timestamp without time zone DEFAULT now() NOT NULL,
    ts_updated timestamp without time zone,
    ts_deleted timestamp without time zone,
    source_device_id integer NOT NULL,
    meas_device_id integer
);


ALTER TABLE ep_event OWNER TO postgres;

--
-- Name: TABLE ep_event; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE ep_event IS 'Az általános esemény entittás. Minden naplózott adat egy eseményhez kötheto. Ha az adatot törlik vagy módosítják, akkor nem jön létre új esemény.';


--
-- Name: COLUMN ep_event.event_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.event_type_code IS 'kódkészlet #2 pl. "étkezési tétel bejegyzés"';


--
-- Name: COLUMN ep_event.status_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.status_code IS 'kódkészlet #3 pl. "módosított"';


--
-- Name: COLUMN ep_event.ts_specified; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.ts_specified IS 'az adatot generáló esemény tényleges ideje a felhasználó szerint, pl. a vércukormérés ideje 7 óra, rögzítés a telefonon 8 óra (ts_recorded), központi adatbázisba mentés 9 óra (ts_received)';


--
-- Name: COLUMN ep_event.ts_recorded; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.ts_recorded IS 'a keletkezés ideje';


--
-- Name: COLUMN ep_event.ts_received; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.ts_received IS 'az adatbázisba érkezés ideje';


--
-- Name: COLUMN ep_event.ts_updated; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.ts_updated IS 'a felhasználói módosítás ideje';


--
-- Name: COLUMN ep_event.ts_deleted; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.ts_deleted IS 'a logikai törlés ideje';


--
-- Name: COLUMN ep_event.source_device_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.source_device_id IS 'Az adatot beküldo eszköz. Az adatok általában mobil eszközökön keletkeznek, speciális eszköz pl. a webes bevitel.';


--
-- Name: COLUMN ep_event.meas_device_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN ep_event.meas_device_id IS 'A méroeszköz, ha az adatot szolgáltató mérés valamilyen ismert típusú eszközzel, pl. vércukormérovel történt';


--
-- Name: ep_event_event_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE ep_event_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ep_event_event_id_seq OWNER TO postgres;

--
-- Name: ep_event_event_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE ep_event_event_id_seq OWNED BY ep_event.event_id;


--
-- Name: episode; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE episode (
    episode_id integer NOT NULL,
    user_id integer NOT NULL,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone,
    ep_type_code smallint NOT NULL
);


ALTER TABLE episode OWNER TO postgres;

--
-- Name: COLUMN episode.episode_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN episode.episode_id IS 'egy felhasználóhoz köthető adatgyűjtési szakasz (időtartomány)';


--
-- Name: COLUMN episode.ep_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN episode.ep_type_code IS 'kódkészlet #1 pl. "fizetős Lavinia felhasználás"';


--
-- Name: episode_device; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE episode_device (
    episode_id integer NOT NULL,
    device_id integer NOT NULL,
    device_type_code smallint
);


ALTER TABLE episode_device OWNER TO postgres;

--
-- Name: COLUMN episode_device.device_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN episode_device.device_type_code IS 'kódkészlet #10 pl. ''okostelefon'', ''külső HW vércukormérő'' stb';


--
-- Name: episode_episode_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE episode_episode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE episode_episode_id_seq OWNER TO postgres;

--
-- Name: episode_episode_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE episode_episode_id_seq OWNED BY episode.episode_id;


--
-- Name: event_anamnesis; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_anamnesis (
    event_id integer NOT NULL,
    height integer,
    weight real,
    birth_date date,
    gender_code smallint,
    lifestyle_code smallint,
    sport_code smallint,
    mass_change real,
    mass_change_time integer,
    egfr real,
    steorid_treatment boolean,
    insulin_dose real
);


ALTER TABLE event_anamnesis OWNER TO postgres;

--
-- Name: TABLE event_anamnesis; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_anamnesis IS 'az anamnézis bevitt és számított mezői';


--
-- Name: COLUMN event_anamnesis.height; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.height IS 'cm';


--
-- Name: COLUMN event_anamnesis.weight; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.weight IS 'kg';


--
-- Name: COLUMN event_anamnesis.gender_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.gender_code IS 'kódkészlet #7, "férfi", "nő"';


--
-- Name: COLUMN event_anamnesis.lifestyle_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.lifestyle_code IS 'kódkészlet #8, pl. "irodai munka"';


--
-- Name: COLUMN event_anamnesis.sport_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.sport_code IS 'kódkészlet #9, pl. "nincs"';


--
-- Name: COLUMN event_anamnesis.mass_change; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.mass_change IS 'kg';


--
-- Name: COLUMN event_anamnesis.mass_change_time; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.mass_change_time IS 'hónap';


--
-- Name: COLUMN event_anamnesis.egfr; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.egfr IS 'labor';


--
-- Name: COLUMN event_anamnesis.steorid_treatment; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.steorid_treatment IS 'labor';


--
-- Name: COLUMN event_anamnesis.insulin_dose; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis.insulin_dose IS 'labor';


--
-- Name: event_anamnesis_illness; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_anamnesis_illness (
    anamnesis_id integer NOT NULL,
    illness_id integer NOT NULL,
    parameter character varying,
    param_label_id integer
);


ALTER TABLE event_anamnesis_illness OWNER TO postgres;

--
-- Name: TABLE event_anamnesis_illness; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_anamnesis_illness IS 'a felhasználó ismert betegségei';


--
-- Name: COLUMN event_anamnesis_illness.parameter; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis_illness.parameter IS 'pl. "inzulinnal kezelt" a II. típusú cukorbetegségnél, vagy "nátriumoxalát kő" a vesekőnél. A numerikus paramétereket (pl. 160 g-os cukorbetegségnél a 160 g CH) az RDA értékek editálhatósága révén mentjük.';


--
-- Name: COLUMN event_anamnesis_illness.param_label_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_anamnesis_illness.param_label_id IS '

A paraméter szövegét tartalmazó label_id.

Az alkalmazás a betegséglista lekérdezésekor kapja meg.';


--
-- Name: event_anamnesis_rda; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_anamnesis_rda (
    anamnesis_id integer NOT NULL,
    nutr_id integer NOT NULL,
    min_value real NOT NULL,
    opt_value real NOT NULL,
    max_value real NOT NULL,
    unit_id integer NOT NULL,
    unit_label character varying NOT NULL
);


ALTER TABLE event_anamnesis_rda OWNER TO postgres;

--
-- Name: TABLE event_anamnesis_rda; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_anamnesis_rda IS 'a tápanyagonként számított RDA/NBÉ';


--
-- Name: event_bp_meas; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_bp_meas (
    event_id integer NOT NULL,
    systolic_data integer,
    diastolic_data integer,
    pulse_data integer
);


ALTER TABLE event_bp_meas OWNER TO postgres;

--
-- Name: event_comment; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_comment (
    event_id integer NOT NULL,
    author_id integer NOT NULL,
    author_role_code smallint NOT NULL,
    comment_text text
);


ALTER TABLE event_comment OWNER TO postgres;

--
-- Name: TABLE event_comment; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_comment IS 'felhasználói megjegyzés elhelyezhető egy tételre, étkezésre vagy egy napra, a saját naplóban vagy a fogyótárs/páciens naplójában. Egy tételre egy felhasználó csak egy megjegyzést tehet, de ez később bővíthető.';


--
-- Name: COLUMN event_comment.author_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_comment.author_id IS 'A létrehozó felhasználó';


--
-- Name: COLUMN event_comment.author_role_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_comment.author_role_code IS 'kódkészlet #4 pl. "fogyótárs" vagy "orvos"';


--
-- Name: event_glucose_meas; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_glucose_meas (
    event_id integer NOT NULL,
    meas_time_code smallint,
    glucose_data real
);


ALTER TABLE event_glucose_meas OWNER TO postgres;

--
-- Name: COLUMN event_glucose_meas.meas_time_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_glucose_meas.meas_time_code IS 'kódkészlet #17 pl. éhomi, étkezés előtti stb';


--
-- Name: event_item_content; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_item_content (
    item_id integer NOT NULL,
    nutr_id integer NOT NULL,
    quantity real NOT NULL
);


ALTER TABLE event_item_content OWNER TO postgres;

--
-- Name: TABLE event_item_content; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_item_content IS 'egy tétel tápanyagtartalma';


--
-- Name: COLUMN event_item_content.quantity; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_item_content.quantity IS 'a GI is egy speciális nutrient. csak a tétel átlagolt GI-ét tároljuk';


--
-- Name: event_meal; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_meal (
    event_id integer NOT NULL,
    ts_meal_end timestamp without time zone,
    meal_type_code smallint,
    glyc_load real
);


ALTER TABLE event_meal OWNER TO postgres;

--
-- Name: TABLE event_meal; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_meal IS 'Egy étkezés, pl. egy ebéd. Tartozhat hozzá comment, glikémikás terhelés stb. és általában vannak tételei.';


--
-- Name: COLUMN event_meal.ts_meal_end; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_meal.ts_meal_end IS 'az étkezés vége, ha a felhasználó megadta. Az étkezés kezdete a felhasználó szerint az event.ts_specified';


--
-- Name: COLUMN event_meal.meal_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_meal.meal_type_code IS 'kódkészlet #5 pl. "uzsonna" Ha egy napon több "egyéb" étkezés van, ezek külön event_meal rekordokként jelenhetnek meg, ha a felhasználó által megadott étkezési idő különbözik. Ettől függetlenül a felületen egyetlen "Egyéb" csoportban szerepelnek.';


--
-- Name: event_mealitem; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_mealitem (
    event_id integer NOT NULL,
    item_type_code smallint NOT NULL,
    food_id integer,
    recipe_id integer,
    item_label character varying,
    meal_id integer NOT NULL,
    quantity real NOT NULL,
    unit_id integer NOT NULL,
    unit_label character varying
);
ALTER TABLE ONLY event_mealitem ALTER COLUMN item_label SET STATISTICS 0;


ALTER TABLE event_mealitem OWNER TO postgres;

--
-- Name: TABLE event_mealitem; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_mealitem IS 'egy étkezési tétel. Tároljuk a rögzítéskor kiszámított tápanyagtartalmat is hozzá, mivel ezek az értékek a dietary adatbázisban közben változhatnak';


--
-- Name: COLUMN event_mealitem.item_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_mealitem.item_type_code IS 'kódkészlet #6, két lehetséges értéke "étel" és "élelmiszer"';


--
-- Name: COLUMN event_mealitem.food_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_mealitem.food_id IS 'a food_id/recipe_id közül pontosan az egyik null';


--
-- Name: COLUMN event_mealitem.unit_label; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_mealitem.unit_label IS 'a gyors listázás érdekében tárolva';


--
-- Name: event_medication; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_medication (
    event_id integer NOT NULL,
    medication_id integer NOT NULL,
    quantity numeric NOT NULL,
    unit_id integer,
    unit_label character varying,
    admin_route_code smallint,
    admin_loc_code smallint,
    related_meal_id integer,
    meal_relation_type_code smallint,
    related_meal_type_code smallint
);


ALTER TABLE event_medication OWNER TO postgres;

--
-- Name: COLUMN event_medication.admin_route_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_medication.admin_route_code IS 'kódkészlet #13 pl. nasal, subcutan... (API 37. o.)';


--
-- Name: COLUMN event_medication.admin_loc_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_medication.admin_loc_code IS 'kódkészlet #14 pl. has, láb elöl, ... (API 37. o.)';


--
-- Name: COLUMN event_medication.related_meal_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_medication.related_meal_id IS 'amelyik étkezéshez bevette';


--
-- Name: COLUMN event_medication.meal_relation_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_medication.meal_relation_type_code IS 'kódkészlet #15 pl. étkezés előtt, étkezés után';


--
-- Name: COLUMN event_medication.related_meal_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_medication.related_meal_type_code IS 'kódkészlet #5 pl. reggeli, ebéd,...';


--
-- Name: event_missing_food; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_missing_food (
    event_id integer NOT NULL,
    food_id integer,
    recipe_id integer,
    message_text character varying(1)
);


ALTER TABLE event_missing_food OWNER TO postgres;

--
-- Name: TABLE event_missing_food; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON TABLE event_missing_food IS 'a felhasználó által kért, az adatbázisból hiányzó étel vagy élelmiszer.

A food_id és a recipe_id közül csak az egyik lehet nem null';


--
-- Name: COLUMN event_missing_food.food_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_missing_food.food_id IS 'ha a kérésre új élelmiszert vittek be, annak az id-je';


--
-- Name: COLUMN event_missing_food.recipe_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_missing_food.recipe_id IS 'ha a kérésre új ételt vittek be, annak az id-je';


--
-- Name: COLUMN event_missing_food.message_text; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_missing_food.message_text IS 'a felhasználó által adott leírás';


--
-- Name: event_physical; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_physical (
    event_id integer NOT NULL,
    pa_id integer NOT NULL,
    pa_label character varying,
    duration integer,
    energy_consumed integer
);


ALTER TABLE event_physical OWNER TO postgres;

--
-- Name: COLUMN event_physical.pa_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_physical.pa_id IS 'a fizikai aktivitás id-je';


--
-- Name: COLUMN event_physical.duration; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_physical.duration IS 'az aktivitás hossza percben';


--
-- Name: COLUMN event_physical.energy_consumed; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN event_physical.energy_consumed IS 'elhasznált energia kcal-ban';


--
-- Name: event_weight_meas; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE event_weight_meas (
    event_id integer NOT NULL,
    weight_data real
);


ALTER TABLE event_weight_meas OWNER TO postgres;

--
-- Name: expert_user; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE expert_user (
    user_id integer NOT NULL,
    organisation character varying
);


ALTER TABLE expert_user OWNER TO postgres;

--
-- Name: COLUMN expert_user.organisation; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN expert_user.organisation IS 'pl. Vanderlich EC';


--
-- Name: group_member; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE group_member (
    user_id integer NOT NULL,
    group_id integer NOT NULL,
    date_start date NOT NULL,
    date_end date,
    internal_group_id character varying,
    external_group_id character varying
);


ALTER TABLE group_member OWNER TO postgres;

--
-- Name: COLUMN group_member.internal_group_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN group_member.internal_group_id IS 'pl. P06';


--
-- Name: COLUMN group_member.external_group_id; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN group_member.external_group_id IS 'pl. KZ2062 (klinikai kísérletben a kórház belső betegazonosítója)';


--
-- Name: lavinia_illness; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE lavinia_illness (
    illness_id integer NOT NULL,
    illness_name text
);


ALTER TABLE lavinia_illness OWNER TO postgres;

--
-- Name: test_illness; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_illness (
    illness_id integer,
    parameter character varying
);


ALTER TABLE test_illness OWNER TO postgres;

--
-- Name: test_lavinia_foods_in_foodsets_list; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_lavinia_foods_in_foodsets_list (
    set_id integer,
    set_name text,
    food_id integer,
    food_name text
);


ALTER TABLE test_lavinia_foods_in_foodsets_list OWNER TO postgres;

--
-- Name: test_lavinia_foodset_list; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_lavinia_foodset_list (
    parent_set_id integer,
    parent_set_name text,
    set_id integer,
    set_name text,
    set_level integer
);


ALTER TABLE test_lavinia_foodset_list OWNER TO postgres;

--
-- Name: test_lavinia_item_list; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_lavinia_item_list (
    id integer,
    type text,
    short_name text,
    long_name text,
    unit_id integer,
    unit_name text,
    unit_long_name text
);


ALTER TABLE test_lavinia_item_list OWNER TO postgres;

--
-- Name: test_lavinia_keszetel_list; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_lavinia_keszetel_list (
    parent_set_id integer,
    parent_set_name text,
    set_id integer,
    set_name text,
    set_level integer
);


ALTER TABLE test_lavinia_keszetel_list OWNER TO postgres;

--
-- Name: test_lavinia_nutrients_in_foodsets_list; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_lavinia_nutrients_in_foodsets_list (
    food_id integer,
    elelm_nev text,
    source_id integer,
    nutr_id integer,
    tapanyag_nev text,
    fc_quantity double precision
);


ALTER TABLE test_lavinia_nutrients_in_foodsets_list OWNER TO postgres;

--
-- Name: test_lavinia_recipe_contents_in_keszetel_list; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_lavinia_recipe_contents_in_keszetel_list (
    set_id integer,
    set_name text,
    rec_id integer,
    recipe_name text,
    food_id integer,
    food_name text,
    rc_quantity double precision,
    unit_id integer,
    operation_id integer,
    operation_name text
);


ALTER TABLE test_lavinia_recipe_contents_in_keszetel_list OWNER TO postgres;

--
-- Name: test_vi_food_cont_source_nevekkel; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE test_vi_food_cont_source_nevekkel (
    elem_id integer,
    elelm_nev text,
    forras_id integer,
    forras_nev text,
    tapanyag_id integer,
    tapanyag_nev text,
    mennyiseg double precision,
    mertegys_id integer,
    mertegys_nev text
);


ALTER TABLE test_vi_food_cont_source_nevekkel OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE "user" (
    user_id integer NOT NULL,
    user_type_code smallint NOT NULL,
    firstname character varying,
    family_name character varying,
    mobile character varying,
    skype character varying,
    email character varying,
    google_account character varying,
    ios_account character varying,
    ds_id character varying,
    lavinia_name character varying,
    default_insulin_type_id integer,
    user_desc character varying,
    illness_type_code smallint
);


ALTER TABLE "user" OWNER TO postgres;

--
-- Name: COLUMN "user".user_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN "user".user_type_code IS 'kódkészlet #11 pl. fejlesztő, szakértő, tényleges felhasználó stb';


--
-- Name: COLUMN "user".illness_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN "user".illness_type_code IS 'kódkészlet #16 pl. I. típusú, II. típusú, egészséges, terhességi, vesebeteg stb.';


--
-- Name: user_group; Type: TABLE; Schema: log; Owner: postgres
--

CREATE TABLE user_group (
    group_id integer NOT NULL,
    group_type_code smallint NOT NULL,
    group_name character varying NOT NULL,
    group_create_date timestamp without time zone,
    expert_id integer
);


ALTER TABLE user_group OWNER TO postgres;

--
-- Name: COLUMN user_group.group_type_code; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN user_group.group_type_code IS 'kódkészlet #12 pl. fogyópár, klinikai kísérlet, edzőtábor';


--
-- Name: COLUMN user_group.group_name; Type: COMMENT; Schema: log; Owner: postgres
--

COMMENT ON COLUMN user_group.group_name IS 'pl. HK4 kísérlet';


--
-- Name: user_group_group_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE user_group_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE user_group_group_id_seq OWNER TO postgres;

--
-- Name: user_group_group_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE user_group_group_id_seq OWNED BY user_group.group_id;


--
-- Name: user_user_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE user_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE user_user_id_seq OWNER TO postgres;

--
-- Name: user_user_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE user_user_id_seq OWNED BY "user".user_id;


--
-- Name: type_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY data_type ALTER COLUMN type_id SET DEFAULT nextval('data_type_type_id_seq'::regclass);


--
-- Name: device_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device ALTER COLUMN device_id SET DEFAULT nextval('device_device_id_seq'::regclass);


--
-- Name: model_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device_model ALTER COLUMN model_id SET DEFAULT nextval('device_model_model_id_seq'::regclass);


--
-- Name: event_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY ep_event ALTER COLUMN event_id SET DEFAULT nextval('ep_event_event_id_seq'::regclass);


--
-- Name: episode_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY episode ALTER COLUMN episode_id SET DEFAULT nextval('episode_episode_id_seq'::regclass);


--
-- Name: user_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY "user" ALTER COLUMN user_id SET DEFAULT nextval('user_user_id_seq'::regclass);


--
-- Name: group_id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY user_group ALTER COLUMN group_id SET DEFAULT nextval('user_group_group_id_seq'::regclass);


--
-- Data for Name: code_item; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY code_item (type_id, item_id, item_name) FROM stdin;
\.


--
-- Data for Name: code_type; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY code_type (type_id, type_name) FROM stdin;
\.


--
-- Data for Name: data_type; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY data_type (type_id, type_name_label_id, default_unit_id) FROM stdin;
\.


--
-- Name: data_type_type_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('data_type_type_id_seq', 1, false);


--
-- Data for Name: device; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY device (device_id, hw_serial, model_id, dev_type_code) FROM stdin;
5028	UUID:a05076b4-a731-4e8c-b3d8-fa7a4a1bb3b2	\N	0
5029	UUID:d2593f52-30ba-4e27-b452-1decf7a508c7	\N	0
5030	UUID:3f86c8dd-4c8a-461c-b851-e4f02ae6ab7f	\N	0
5031	UUID:763625ad-8f77-46c9-813c-349e95212876	\N	0
5032	UUID:9c4b40ad-2a53-468f-b8da-ad792868499c	\N	0
5033	UUID:02821c39-0c0d-432d-8d5e-a96dcf5163cf	\N	0
5034	UUID:51ea154b-e1ae-44b4-ac4d-b9e7bbfc5717	\N	0
5035	UUID:be1127ce-fb8d-4065-942a-de1ba727e0cc	\N	0
5036	UUID:24c38302-ceef-4842-bd28-9c644c24d25f	\N	0
5037	UUID:45c6ec86-93dd-46ff-8a60-fc6a85f9fbc9	\N	0
5038	\N	\N	0
5039	UUID:5e088216-99b1-4efa-8985-db573475f838	\N	0
5040	\N	\N	0
5041		\N	0
5042	\N	\N	0
5043	UUID:7f6b6c59-0e91-43d8-93da-a8c315d827b1	\N	0
5044	UUID:a7f498d7-0a0c-4319-990d-0110783068b7	\N	0
5045	UUID:725e7e8c-c47e-4763-88c6-b100f0a49e3d	\N	0
5046	UUID:ad80b90e-64c5-4067-9e46-ffe66dc8fc2a	\N	0
5047	UUID:22d40468-9ac2-4c29-98fa-85176c02a53f	\N	0
5048	UUID:1beeda53-9c84-4e71-92d1-8d36821986b4	\N	0
5049	\N	\N	0
5050	UUID:f994bd9e-2b8b-4141-b74f-8d11e6387208	\N	0
5051	UUID:8f5ea097-d60b-4182-a1fc-878e44e144eb	\N	0
5052	UUID:b054b9a6-9143-460c-ac64-d51bf77a2a98	\N	0
5053	UUID:fddc4281-d08e-4a1f-9b1b-aa5dd7b1ec6c	\N	0
5054	UUID:0a049c96-def6-468c-ad08-1f23b8abfc46	\N	0
5055	UUID:336d7096-db81-4901-bc04-ef9155e59124	\N	0
5056	UUID:11ad4523-d630-4fbf-afe8-963c3f6f9303	\N	0
5057	UUID:2149bcfd-fc19-4108-98ef-905085340ae1	\N	0
5058	UUID:893dcc2a-e314-4753-b0ab-dd370ca81f7d	\N	0
5059	UUID:96983e82-3ba0-4c15-b5ae-2449dedfd184	\N	0
5060	UUID:2a80bd9c-02b5-4da4-9349-9f4d40ba81bc	\N	0
5061	UUID:c889dda5-4c9a-41b9-9df0-7d6cd1a5dabc	\N	0
5062	UUID:2938a7ff-2914-45b8-ab2c-7a96db304a92	\N	0
5063	UUID:49f45c22-9ff3-4257-af9b-7cb56616a84c	\N	0
5064	UUID:7d14a014-196f-4b50-bfc3-f0c54e83e6b8	\N	0
5065	UUID:3bc57422-deb8-46d0-9646-e64d6e1bab61	\N	0
5066	UUID:155b9c31-ec23-4f98-b035-3134eff18d21	\N	0
5067	UUID:c2ad35bc-12e0-4d41-bd81-a7788f831d37	\N	0
5068	UUID:bec8ab14-afbc-40d9-b634-4ac669f43cbc	\N	0
5069	UUID:00fe123a-52a1-4f01-a200-0d19a03614b9	\N	0
5070	UUID:df2af840-92e0-4cbe-9ada-9afe10a9e753	\N	0
5071	UUID:bddfb68e-cd90-45be-9435-8781fe5ce2de	\N	0
5072	UUID:3952bd07-f3f8-472f-8fb0-23e8e3866a3b	\N	0
5073	UUID:2d820aee-440e-41ea-99bc-8b73b39811c9	\N	0
5074	UUID:bc8628e4-2181-4219-94a0-0e4c706a67de	\N	0
5075	UUID:7ec591a8-9be3-45a1-8fff-130d4dbe9b52	\N	0
5076	UUID:179ab044-29dd-41d0-8b9a-bb510b4a91b5	\N	0
5077	UUID:373b8969-3cdd-4789-848b-1405204e350a	\N	0
5078	UUID:38c1be1a-5e1a-4f74-9d7a-0083add9017c	\N	0
5079	UUID:b4fce509-35c6-4ba5-8d1c-634d02aacbea	\N	0
5080	UUID:a60d6d2d-e316-44e9-989e-31e152899fc4	\N	0
5081	UUID:a47809b9-be11-459a-95d2-5a3a49a84e46	\N	0
5082	UUID:a5daecd5-9d74-4d7c-baeb-c20451719e73	\N	0
5083	UUID:2eb0bafd-9ca5-4aaa-980e-11071d9e00df	\N	0
5084	UUID:323f39e0-c3b8-41d4-b203-7145580a3dd5	\N	0
5085	UUID:b9497418-86d7-4ef7-a210-44a1eaea024a	\N	0
5086	UUID:f9279e0b-3590-4956-97ab-6534c4574aba	\N	0
5087	UUID:e2aa65ab-dd45-4ade-96db-bb39f68662be	\N	0
5088	UUID:26d5eaf2-4ce9-4a70-b950-61c0f53104f5	\N	0
5089	UUID:9cab909d-afd0-4584-8c30-c969ab8a0dd3	\N	0
5090	UUID:b56de4d0-c2a1-454d-bbd3-b6a51e20996d	\N	0
5091	UUID:3b2effef-9414-4944-8f26-64977b0557fb	\N	0
5092	UUID:245b9f09-5db2-4732-bad3-bfdbc1f9702b	\N	0
5093	UUID:0d6e6616-98ce-400c-994b-3c731ac0580d	\N	0
5094	UUID:61375c93-010f-41b0-b556-7ada42ea3759	\N	0
5095	UUID:af05deb7-8716-49f3-b3ba-24eaeb058ce6	\N	0
5096	UUID:0faefa87-0eeb-4be0-8429-0600a9865e83	\N	0
5097	UUID:0dcba527-fa38-464e-8cf1-87c5ec4e72d3	\N	0
5098	UUID:af47d721-4a68-4785-b6c7-a95da3898945	\N	0
5099	UUID:22d36c09-bf28-4ab6-b31b-b072fb759d20	\N	0
5100	UUID:60540185-41be-4396-8edf-ce78d5bb8854	\N	0
5101	UUID:6c14eb58-696c-4bd4-95cd-7277fbf865bd	\N	0
5102	UUID:443df5b0-1bee-4876-b9f9-a25e609b63d7	\N	0
5103	UUID:a498ac13-c626-4dd4-a2de-ab82fcc37c00	\N	0
5104	UUID:e21e5697-a1eb-4f6f-9ad5-1457ca746b4b	\N	0
5105	UUID:25b1a963-bbb4-4800-83ca-2ae3d83a9696	\N	0
5106	UUID:0b1872f9-f540-42b2-8de1-68b3f990ed0d	\N	0
5107	UUID:45cf7f20-a387-4714-8458-e535724f69bd	\N	0
5108	UUID:6e8f8b02-0dd1-4e7b-b077-96aa04e8253e	\N	0
5109	UUID:ff7bdf9f-eea1-47ed-9f21-459d5bc426d1	\N	0
5110	UUID:9cb0abc1-9e61-4d56-b2db-12b05021029f	\N	0
5111	UUID:4b349c2d-4efb-4298-ab4e-aef3218c01ca	\N	0
5112	UUID:e770428f-0489-45d5-961a-7c6456e785b6	\N	0
5113	UUID:7adc7561-5367-40a4-b8c3-33f2811bcdca	\N	0
5114	UUID:2f40ae8d-2641-4567-a7de-4b66a610d2dd	\N	0
5115	UUID:cc72a3e3-59da-40ed-90e0-326040c014cf	\N	0
5116	UUID:0829a9e9-3098-4b88-a2ee-83d4c7f22cf0	\N	0
5117	UUID:4938d5d5-d41b-40ed-a6b2-84a7be6a7cee	\N	0
5118	UUID:5140c4d1-92b6-4763-9a3e-285a0d8bd3dc	\N	0
5119	UUID:cb41a252-393c-461f-9bbf-324ac2042cbd	\N	0
5120	UUID:38a55fe6-fa10-46c8-98cb-7d6d753e7471	\N	0
5121	UUID:a21fedd4-528e-4ec7-b1cb-62ed3effbc4f	\N	0
5122	UUID:c403dc51-0238-4a8f-9125-56a183062ac7	\N	0
5123	UUID:3d529577-3729-4936-86fe-92dd5de9148d	\N	0
5124	UUID:e33544bb-8707-4959-b43d-8802f595d202	\N	0
5125	UUID:ca6fc087-2b37-4b4e-bc5d-a62bc448d5fe	\N	0
5126	UUID:0ef5dd30-2c68-4cf3-ac6c-673eb56ca779	\N	0
5127	UUID:be749a24-d25d-48ce-bd41-1485a3f01072	\N	0
5128	UUID:33f79903-fb2b-4543-a72b-9651c842bf05	\N	0
5129	UUID:a66c05e3-4486-4b7e-b402-105069353675	\N	0
5130	UUID:ce8bb149-f729-4bf6-946a-50de7be724d3	\N	0
5131	UUID:8284724d-e791-4bc5-91b8-5b9471a7d0e7	\N	0
5132	UUID:4c3533e5-ced8-401f-b4d7-2d1873e6d53d	\N	0
5133	UUID:5929b4bc-32bc-4f9f-aff2-913825a13c8c	\N	0
5134	UUID:8f9bdc3d-5add-4780-b71c-129ade2bd940	\N	0
5135	UUID:cd2d73f8-fd65-4c7b-bfec-4997f4751d98	\N	0
5136	UUID:869e2c7a-9fc3-4d01-9786-e7967a287de0	\N	0
5137	UUID:b02e4cb3-24fc-4aa6-b67a-7de61d4b3925	\N	0
5138	UUID:b7ebbc7a-2785-4afd-8b92-c51f770c0d14	\N	0
5139	UUID:f010e9d8-5220-485c-9a27-87e39532d807	\N	0
5140	UUID:573f4a21-7783-49bf-b0ee-f56875cf39bb	\N	0
5141	UUID:6bd27fa6-efaf-47e6-935f-0b59ce440ce7	\N	0
5142	UUID:69fb812c-15ce-456c-a6f4-bc021f4ee8c8	\N	0
5143	UUID:8259998b-8af3-4829-912b-e1fa3fa8dfd2	\N	0
5144	UUID:f232675d-3409-4189-b7b5-df54b4361fcc	\N	0
5145	UUID:80f9a1a9-b6e9-4244-aeb2-7279710e3ab4	\N	0
5146	UUID:285e1f6f-ab0c-4441-960e-b27e80ee8613	\N	0
5147	UUID:7e313912-cc57-4427-a987-f70b0787ed1f	\N	0
5148	UUID:cd96319a-deaa-4260-9122-c094743f3028	\N	0
5149	UUID:868114d1-6081-4f00-a842-e42b73ab8635	\N	0
5150	UUID:2a08d8d7-3e4f-4a23-9739-94a89b53be8b	\N	0
5151	UUID:ee2d1299-ea27-4bee-971b-010305d06923	\N	0
5152	UUID:4fb4a3c8-3903-40e3-a342-dd3e14eafd1e	\N	0
5153	UUID:32885d05-b3a6-4621-a34e-c4ea97e7e28c	\N	0
5154	UUID:566117fb-2836-433e-88a3-1f6cc286cc74	\N	0
5155	UUID:0404c747-df79-4b54-89ef-aa8689c0fa81	\N	0
5156	UUID:49daa9ca-6e9b-4164-adfe-e71c9930112d	\N	0
5157	UUID:27d60732-ae2c-4784-a874-699fc20cbada	\N	0
5158	UUID:7f805e96-e419-48a7-9e0e-ba0fe3a265f5	\N	0
5159	UUID:37ffa328-f888-4622-b36c-c412b885d174	\N	0
5160	UUID:d82dcb36-0bd7-469c-a956-263bb1b85781	\N	0
5161	UUID:24cc4535-cedc-4eb2-b440-a1540927b68f	\N	0
5162	UUID:ce78da9c-a832-4bb3-a178-b910fa5abc87	\N	0
5163	UUID:a990f830-f407-4f41-96ea-bda5881bc9ef	\N	0
5164	UUID:719aeb0c-2598-4232-a72d-006757fe17c9	\N	0
5165	UUID:0a3bc711-f0ed-4f5a-945f-9ce79b3aec0a	\N	0
5166	UUID:54a964bb-5694-4725-beb9-9442ca70295e	\N	0
5167	UUID:03ece8df-4212-4df6-8803-73edc631d578	\N	0
5168	UUID:83e3a882-b2a9-461e-9960-b47b3e133788	\N	0
5169	UUID:26c25a03-3465-4eca-8194-6631bd4784f9	\N	0
5170	UUID:414edd0e-a5d0-4014-81fb-0194a4fc046c	\N	0
5171	UUID:07d3e70f-f647-4c77-9b6b-b354826b94b1	\N	0
5172	UUID:7906070b-4866-4318-b60c-42eb4c22ec7a	\N	0
5173	UUID:69acc17d-2c40-4adc-a749-0c4457f50a28	\N	0
5174	UUID:683f1fb2-4f02-4dd2-a00e-b88dfb35f246	\N	0
5175	UUID:4ee6b895-eef3-45ef-bed0-bd027ac0ba07	\N	0
5176	UUID:9fdc4943-9a10-4ca0-b78f-618192acd72c	\N	0
5177	UUID:77836a77-65a8-46bc-9806-e55567df96a7	\N	0
5178	UUID:eca43722-71fa-4578-b367-6803e74e89cc	\N	0
5179	UUID:fcb87fe2-4a8a-485e-9d5d-bec2bbea8029	\N	0
5180	UUID:8d9b2060-ebdc-4049-a5e2-6737b5ec4eea	\N	0
5181	UUID:d1c78615-6d4a-4933-b996-f0b5fece0f6e	\N	0
5182	UUID:2cecb947-76f2-4b46-bcbb-4d34fb4bd51b	\N	0
5183	UUID:ece210c1-e2ce-4d77-9ebb-bd35007e3e77	\N	0
5184	UUID:0ecf5b84-5176-4a0e-8baf-d93c9aa9fea9	\N	0
5185	UUID:6d29361a-5a58-4fe8-8b9b-0767f011d0b4	\N	0
5186	UUID:ca34007e-9dea-44b4-ade1-8561417d54d8	\N	0
5187	UUID:adf73e6d-b973-41e7-9894-dfca4c00ae25	\N	0
5188	UUID:ef0ade13-3d6a-4a87-ad71-a69e7178745d	\N	0
5189	UUID:b30f696a-d52c-46e6-94d3-b603dbd0ff3f	\N	0
5190	UUID:985a834a-cb2b-4755-9d76-add1de213c67	\N	0
5191	UUID:94677875-ffb0-429f-8d6e-9484ec9c3b07	\N	0
5192	UUID:c5e3082a-dd85-4db5-8862-af2d8778cddb	\N	0
5193	UUID:523223c0-975d-45f4-bbc5-b3355d1bad0a	\N	0
5194	UUID:012125d6-d597-443f-9d6d-27e6492cde02	\N	0
5195	UUID:841321df-ef16-4a90-bca0-080d40d7a537	\N	0
5196	UUID:2a6bceb6-c3b3-4669-b90d-9c01f414305d	\N	0
5197	UUID:d22e1b3c-40d3-4aa1-8f6d-a497bbf91c4f	\N	0
5198	UUID:c361e97d-d3f5-4c78-ab49-0d3c073d7225	\N	0
5199	UUID:6549d607-bec2-40f2-aa30-5786c59f8f95	\N	0
5200	UUID:e677ee8c-7a6d-44e7-86c2-e847f5b630eb	\N	0
5201	UUID:b8915058-8db0-4c18-bcca-3dfdbf271273	\N	0
5202	UUID:494b4126-4425-4716-a4c2-cff3bb3a4e7f	\N	0
5203	UUID:ccd15271-74e9-4566-8be6-ee6cc839d02f	\N	0
5204	UUID:d66054be-3af6-4c73-88f0-b21a05b27482	\N	0
5205	UUID:efd807e6-9fcd-47fc-b9ef-9a220849931d	\N	0
5206	UUID:c412e988-38fa-46d2-ada4-f79734ddfcdd	\N	0
5207	UUID:d36ce08e-02d1-41e8-9d20-65579aa22442	\N	0
5208	UUID:6e7f569b-1e73-4367-8b93-a3f563359c7d	\N	0
5209	UUID:6d1d1f91-57c5-4dcc-8ab6-3b36559a81fc	\N	0
5210	UUID:69c9027a-aaab-47bd-b7af-e781eed4be9b	\N	0
5211	UUID:64601fb9-192f-4eb6-8bad-92755c4d60f0	\N	0
5212	UUID:20404bdf-17a8-451b-9ea8-aaad08f7c8c0	\N	0
5213	UUID:1fa02b72-f12f-4607-a4f4-ff1287dbd9a4	\N	0
5214	UUID:93cd011a-eeca-4ec5-b1ca-a9980b8729fc	\N	0
5215	UUID:58a099e6-8950-4341-95e1-6c5ab98f8b0d	\N	0
5216	UUID:59f0de51-4f0f-489c-b3bd-3f0040e8bfe8	\N	0
5217	UUID:14ccef1c-0be9-4f60-ae39-2d463788bc11	\N	0
5218	UUID:662760c9-fdd1-49b4-85c9-22015e648799	\N	0
5219	UUID:f130643b-2b73-42d4-b2e0-4396c6dcda7e	\N	0
5220	UUID:6e9bb61c-ae0d-4bd4-b161-d1cad363ec8a	\N	0
\.


--
-- Data for Name: device_data_type; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY device_data_type (model_id, type_id, "precision", unit_id) FROM stdin;
\.


--
-- Name: device_device_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('device_device_id_seq', 5220, true);


--
-- Data for Name: device_model; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY device_model (model_id, model_name_label_id) FROM stdin;
\.


--
-- Name: device_model_model_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('device_model_model_id_seq', 1, false);


--
-- Data for Name: ep_event; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY ep_event (event_id, episode_id, event_type_code, status_code, ts_specified, ts_recorded, ts_received, ts_updated, ts_deleted, source_device_id, meas_device_id) FROM stdin;
\.


--
-- Name: ep_event_event_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('ep_event_event_id_seq', 1, false);


--
-- Data for Name: episode; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY episode (episode_id, user_id, start_date, end_date, ep_type_code) FROM stdin;
6938	7017	2012-07-20 00:00:00	\N	1
6939	7018	2012-07-25 00:00:00	\N	1
6940	7019	2016-05-07 00:00:00	\N	1
6941	7020	2013-04-14 00:00:00	\N	1
6942	7021	2013-04-12 00:00:00	\N	1
6943	7022	2013-05-08 00:00:00	\N	1
6944	7023	2013-04-14 00:00:00	\N	1
6945	7024	2013-04-16 00:00:00	\N	1
6946	7025	2014-01-09 00:00:00	\N	1
6947	7026	2013-05-16 00:00:00	\N	1
6948	7027	2013-03-18 00:00:00	\N	1
6949	7028	2013-04-08 00:00:00	\N	1
6950	7029	2013-05-23 00:00:00	\N	1
6951	7030	2013-10-15 00:00:00	\N	1
6952	7031	2013-11-19 00:00:00	\N	1
6953	7032	2013-04-03 00:00:00	\N	1
6954	7033	2014-01-13 00:00:00	\N	1
6955	7034	2013-07-24 00:00:00	\N	1
6956	7035	2013-08-21 00:00:00	\N	1
6957	7036	2013-06-03 00:00:00	\N	1
6958	7037	2016-05-07 00:00:00	\N	1
6959	7038	2013-10-15 00:00:00	\N	1
6960	7039	2013-10-24 00:00:00	\N	1
6961	7040	2013-09-17 00:00:00	\N	1
6962	7041	2013-06-17 00:00:00	\N	1
6963	7042	2013-06-26 00:00:00	\N	1
6964	7043	2013-07-07 00:00:00	\N	1
6965	7044	2016-05-07 00:00:00	\N	1
6966	7045	2016-05-07 00:00:00	\N	1
6967	7046	2013-11-21 00:00:00	\N	1
6968	7047	2016-05-07 00:00:00	\N	1
6969	7048	2016-05-07 00:00:00	\N	1
6970	7049	2016-05-07 00:00:00	\N	1
6971	7050	2013-11-07 00:00:00	\N	1
6972	7051	2013-10-30 00:00:00	\N	1
6973	7052	2013-11-05 00:00:00	\N	1
6974	7053	2013-10-31 00:00:00	\N	1
6975	7054	2016-05-07 00:00:00	\N	1
6976	7055	2016-05-07 00:00:00	\N	1
6977	7056	2013-11-23 00:00:00	\N	1
6978	7057	2016-05-07 00:00:00	\N	1
6979	7058	2016-05-07 00:00:00	\N	1
6980	7059	2013-11-01 00:00:00	\N	1
6981	7060	2013-11-02 00:00:00	\N	1
6982	7061	2016-05-07 00:00:00	\N	1
6983	7062	2016-05-07 00:00:00	\N	1
6984	7063	2016-05-07 00:00:00	\N	1
6985	7064	2016-05-07 00:00:00	\N	1
6986	7065	2016-05-07 00:00:00	\N	1
6987	7066	2016-05-07 00:00:00	\N	1
6988	7067	2016-05-07 00:00:00	\N	1
6989	7068	2014-01-12 00:00:00	\N	1
6990	7069	2016-05-07 00:00:00	\N	1
6991	7070	2014-01-16 00:00:00	\N	1
6992	7071	2014-01-16 00:00:00	\N	1
6993	7072	2014-01-25 00:00:00	\N	1
6994	7073	2014-02-07 00:00:00	\N	1
6995	7074	2014-02-19 00:00:00	\N	1
6996	7075	2014-02-03 00:00:00	\N	1
6997	7076	2014-02-01 00:00:00	\N	1
6998	7077	2014-01-20 00:00:00	\N	1
6999	7078	2016-05-07 00:00:00	\N	1
7000	7079	2014-02-18 00:00:00	\N	1
7001	7080	2014-02-21 00:00:00	\N	1
7002	7081	2014-03-06 00:00:00	\N	1
7003	7082	2014-02-26 00:00:00	\N	1
7004	7083	2014-06-18 00:00:00	\N	1
7005	7084	2014-03-10 00:00:00	\N	1
7006	7085	2014-03-27 00:00:00	\N	1
7007	7086	2014-03-27 00:00:00	\N	1
7008	7087	2014-04-04 00:00:00	\N	1
7009	7088	2014-03-12 00:00:00	\N	1
7010	7089	2014-03-12 00:00:00	\N	1
7011	7090	2014-03-14 00:00:00	\N	1
7012	7091	2014-03-15 00:00:00	\N	1
7013	7092	2016-05-07 00:00:00	\N	1
7014	7093	2014-05-13 00:00:00	\N	1
7015	7094	2014-05-28 00:00:00	\N	1
7016	7095	2014-06-06 00:00:00	\N	1
7017	7096	2014-06-16 00:00:00	\N	1
7018	7097	2014-06-26 00:00:00	\N	1
7019	7098	2014-07-07 00:00:00	\N	1
7020	7099	2014-05-26 00:00:00	\N	1
7021	7100	2014-05-19 00:00:00	\N	1
7022	7101	2014-05-30 00:00:00	\N	1
7023	7102	2014-07-11 00:00:00	\N	1
7024	7103	2016-05-07 00:00:00	\N	1
7025	7104	2014-07-16 00:00:00	\N	1
7026	7105	2014-08-04 00:00:00	\N	1
7027	7106	2014-07-12 00:00:00	\N	1
7028	7107	2014-07-17 00:00:00	\N	1
7029	7108	2014-07-18 00:00:00	\N	1
7030	7109	2014-08-07 00:00:00	\N	1
7031	7110	2014-07-22 00:00:00	\N	1
7032	7111	2016-05-07 00:00:00	\N	1
7033	7112	2014-08-07 00:00:00	\N	1
7034	7113	2014-08-08 00:00:00	\N	1
7035	7114	2014-08-20 00:00:00	\N	1
7036	7115	2014-08-27 00:00:00	\N	1
7037	7116	2014-11-17 00:00:00	\N	1
7038	7117	2014-08-29 00:00:00	\N	1
7039	7118	2014-09-10 00:00:00	\N	1
7040	7119	2014-09-02 00:00:00	\N	1
7041	7120	2014-09-08 00:00:00	\N	1
7042	7121	2014-09-10 00:00:00	\N	1
7043	7122	2014-09-23 00:00:00	\N	1
7044	7123	2014-09-11 00:00:00	\N	1
7045	7124	2014-10-16 00:00:00	\N	1
7046	7125	2014-09-22 00:00:00	\N	1
7047	7126	2014-09-22 00:00:00	\N	1
7048	7127	2014-10-14 00:00:00	\N	1
7049	7128	2014-10-23 00:00:00	\N	1
7050	7129	2016-05-07 00:00:00	\N	1
7051	7130	2014-09-20 00:00:00	\N	1
7052	7131	2014-09-30 00:00:00	\N	1
7053	7132	2014-10-13 00:00:00	\N	1
7054	7133	2014-10-01 00:00:00	\N	1
7055	7134	2014-10-13 00:00:00	\N	1
7056	7135	2014-11-17 00:00:00	\N	1
7057	7136	2016-05-07 00:00:00	\N	1
7058	7137	2014-12-03 00:00:00	\N	1
7059	7138	2016-05-07 00:00:00	\N	1
7060	7139	2014-10-15 00:00:00	\N	1
7061	7140	2014-11-12 00:00:00	\N	1
7062	7141	2016-05-07 00:00:00	\N	1
7063	7142	2014-10-24 00:00:00	\N	1
7064	7143	2014-10-27 00:00:00	\N	1
7065	7144	2014-10-27 00:00:00	\N	1
7066	7145	2014-10-27 00:00:00	\N	1
7067	7146	2014-10-27 00:00:00	\N	1
7068	7147	2014-12-03 00:00:00	\N	1
7069	7148	2014-12-17 00:00:00	\N	1
7070	7149	2014-12-01 00:00:00	\N	1
7071	7150	2014-11-30 00:00:00	\N	1
7072	7151	2015-01-14 00:00:00	\N	1
7073	7152	2014-12-01 00:00:00	\N	1
7074	7153	2014-12-17 00:00:00	\N	1
7075	7154	2014-12-02 00:00:00	\N	1
7076	7155	2014-11-03 00:00:00	\N	1
7077	7156	2014-11-10 00:00:00	\N	1
7078	7157	2016-05-07 00:00:00	\N	1
7079	7158	2014-11-24 00:00:00	\N	1
7080	7159	2014-11-27 00:00:00	\N	1
7081	7160	2014-11-27 00:00:00	\N	1
7082	7161	2014-12-02 00:00:00	\N	1
7083	7162	2014-11-28 00:00:00	\N	1
7084	7163	2014-12-05 00:00:00	\N	1
7085	7164	2014-12-17 00:00:00	\N	1
7086	7165	2015-01-14 00:00:00	\N	1
7087	7166	2014-12-08 00:00:00	\N	1
7088	7167	2014-12-08 00:00:00	\N	1
7089	7168	2015-02-12 00:00:00	\N	1
7090	7169	2015-02-07 00:00:00	\N	1
7091	7170	2016-05-07 00:00:00	\N	1
7092	7171	2014-12-18 00:00:00	\N	1
7093	7172	2014-12-14 00:00:00	\N	1
7094	7173	2014-12-14 00:00:00	\N	1
7095	7174	2014-12-15 00:00:00	\N	1
7096	7175	2016-05-07 00:00:00	\N	1
7097	7176	2014-12-18 00:00:00	\N	1
7098	7177	2014-12-18 00:00:00	\N	1
7099	7178	2014-12-18 00:00:00	\N	1
7100	7179	2014-12-18 00:00:00	\N	1
7101	7180	2014-12-26 00:00:00	\N	1
7102	7181	2015-01-06 00:00:00	\N	1
7103	7182	2015-01-07 00:00:00	\N	1
7104	7183	2014-12-26 00:00:00	\N	1
7105	7184	2015-02-13 00:00:00	\N	1
7106	7185	2015-01-14 00:00:00	\N	1
7107	7186	2016-05-07 00:00:00	\N	1
7108	7187	2015-01-14 00:00:00	\N	1
7109	7188	2015-02-10 00:00:00	\N	1
7110	7189	2016-05-07 00:00:00	\N	1
7111	7190	2016-05-07 00:00:00	\N	1
7112	7191	2015-01-16 00:00:00	\N	1
7113	7192	2015-01-28 00:00:00	\N	1
7114	7193	2015-01-16 00:00:00	\N	1
7115	7194	2015-01-29 00:00:00	\N	1
7116	7195	2015-01-27 00:00:00	\N	1
7117	7196	2015-01-30 00:00:00	\N	1
7118	7197	2016-05-07 00:00:00	\N	1
7119	7198	2015-02-04 00:00:00	\N	1
7120	7199	2016-05-07 00:00:00	\N	1
7121	7200	2015-02-05 00:00:00	\N	1
7122	7201	2015-02-04 00:00:00	\N	1
7123	7202	2015-02-06 00:00:00	\N	1
7124	7203	2015-02-07 00:00:00	\N	1
7125	7204	2015-02-16 00:00:00	\N	1
7126	7205	2016-05-07 00:00:00	\N	1
7127	7206	2015-03-01 00:00:00	\N	1
7128	7207	2016-05-07 00:00:00	\N	1
7129	7208	2015-02-27 00:00:00	\N	1
7130	7209	2016-05-07 00:00:00	\N	1
7131	7210	2014-12-27 00:00:00	\N	1
7132	7211	2015-01-05 00:00:00	\N	1
7133	7212	2016-05-07 00:00:00	\N	1
\.


--
-- Data for Name: episode_device; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY episode_device (episode_id, device_id, device_type_code) FROM stdin;
6992	5079	1
6974	5068	1
7105	5195	1
7062	5150	1
6953	5043	1
7043	5131	1
7044	5130	1
7074	5184	1
7019	5110	1
7028	5115	1
6957	5037	1
7018	5107	1
6940	5038	1
7107	5196	1
6994	5081	1
7084	5169	1
6980	5069	1
7057	5143	1
7103	5194	1
7009	5108	1
7050	5136	1
7007	5094	1
7118	5205	1
7100	5186	1
7091	5176	1
6975	5051	1
6959	5048	1
7006	5093	1
7048	5137	1
7086	5172	1
7020	5103	1
7008	5095	1
7060	5147	1
6966	5056	1
6970	5061	1
7002	5090	1
7129	5218	1
7051	5138	1
7083	5166	1
7001	5088	1
7035	5122	1
6965	5052	1
6977	5064	1
7076	5162	1
7012	5098	1
7056	5144	1
7088	5171	1
7075	5161	1
6984	5073	1
7034	5121	1
6945	5032	1
6972	5062	1
6948	5041	1
6942	5028	1
7127	5216	1
7059	5146	1
7061	5148	1
6950	5046	1
7114	5202	1
7054	5139	1
7073	5160	1
7016	5106	1
7070	5157	1
7021	5102	1
6938	5042	1
7063	5149	1
7064	5151	1
6961	5059	1
7046	5133	1
6954	5039	1
6941	5029	1
7089	5174	1
7124	5213	1
7101	5187	1
7085	5170	1
7045	5132	1
7039	5126	1
7082	5168	1
7033	5120	1
7125	5214	1
7024	5111	1
7108	5197	1
6964	5055	1
7080	5165	1
6956	5047	1
7111	5200	1
6997	5084	1
6960	5050	1
7017	5105	1
6955	5045	1
7072	5159	1
7041	5128	1
7122	5207	1
7025	5112	1
7031	5116	1
7120	5208	1
7029	5117	1
7081	5167	1
6939	5040	1
7119	5206	1
6946	5033	1
6944	5031	1
7092	5177	1
7058	5145	1
7131	5188	1
6958	5049	1
7104	5191	1
7004	5091	1
6988	5077	1
7026	5114	1
7065	5152	1
7109	5198	1
7067	5153	1
7123	5212	1
7115	5210	1
7078	5189	1
7094	5179	1
7095	5180	1
7133	5220	1
6962	5053	1
6978	5065	1
6993	5080	1
6983	5071	1
7047	5135	1
7022	5104	1
7117	5211	1
6999	5086	1
6952	5036	1
7014	5100	1
7087	5173	1
6949	5044	1
7010	5096	1
7112	5201	1
7106	5193	1
7132	5190	1
7121	5209	1
7040	5127	1
6991	5078	1
7003	5089	1
7126	5215	1
7037	5125	1
6989	5076	1
6963	5054	1
7032	5119	1
6996	5083	1
7011	5097	1
7077	5163	1
7102	5192	1
7128	5217	1
7005	5092	1
7023	5109	1
7066	5154	1
7015	5101	1
7038	5124	1
7052	5140	1
7096	5181	1
6973	5067	1
7110	5199	1
7071	5158	1
7079	5164	1
6979	5066	1
6969	5060	1
7042	5129	1
7116	5203	1
6982	5072	1
7098	5183	1
6976	5063	1
6951	5035	1
6995	5082	1
6985	5074	1
7130	5219	1
7027	5113	1
7053	5141	1
7013	5099	1
7093	5178	1
6943	5030	1
6981	5070	1
6971	5058	1
6998	5085	1
6968	5057	1
7090	5175	1
7036	5123	1
7000	5087	1
7049	5134	1
7055	5142	1
7068	5155	1
7030	5118	1
6947	5034	1
7113	5204	1
6967	5075	1
7099	5185	1
7069	5156	1
7097	5182	1
\.


--
-- Name: episode_episode_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('episode_episode_id_seq', 7133, true);


--
-- Data for Name: event_anamnesis; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_anamnesis (event_id, height, weight, birth_date, gender_code, lifestyle_code, sport_code, mass_change, mass_change_time, egfr, steorid_treatment, insulin_dose) FROM stdin;
\.


--
-- Data for Name: event_anamnesis_illness; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_anamnesis_illness (anamnesis_id, illness_id, parameter, param_label_id) FROM stdin;
\.


--
-- Data for Name: event_anamnesis_rda; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_anamnesis_rda (anamnesis_id, nutr_id, min_value, opt_value, max_value, unit_id, unit_label) FROM stdin;
\.


--
-- Data for Name: event_bp_meas; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_bp_meas (event_id, systolic_data, diastolic_data, pulse_data) FROM stdin;
\.


--
-- Data for Name: event_comment; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_comment (event_id, author_id, author_role_code, comment_text) FROM stdin;
\.


--
-- Data for Name: event_glucose_meas; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_glucose_meas (event_id, meas_time_code, glucose_data) FROM stdin;
\.


--
-- Data for Name: event_item_content; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_item_content (item_id, nutr_id, quantity) FROM stdin;
\.


--
-- Data for Name: event_meal; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_meal (event_id, ts_meal_end, meal_type_code, glyc_load) FROM stdin;
\.


--
-- Data for Name: event_mealitem; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_mealitem (event_id, item_type_code, food_id, recipe_id, item_label, meal_id, quantity, unit_id, unit_label) FROM stdin;
\.


--
-- Data for Name: event_medication; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_medication (event_id, medication_id, quantity, unit_id, unit_label, admin_route_code, admin_loc_code, related_meal_id, meal_relation_type_code, related_meal_type_code) FROM stdin;
\.


--
-- Data for Name: event_missing_food; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_missing_food (event_id, food_id, recipe_id, message_text) FROM stdin;
\.


--
-- Data for Name: event_physical; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_physical (event_id, pa_id, pa_label, duration, energy_consumed) FROM stdin;
\.


--
-- Data for Name: event_weight_meas; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY event_weight_meas (event_id, weight_data) FROM stdin;
\.


--
-- Data for Name: expert_user; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY expert_user (user_id, organisation) FROM stdin;
\.


--
-- Data for Name: group_member; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY group_member (user_id, group_id, date_start, date_end, internal_group_id, external_group_id) FROM stdin;
\.


--
-- Data for Name: lavinia_illness; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY lavinia_illness (illness_id, illness_name) FROM stdin;
\.


--
-- Data for Name: test_illness; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_illness (illness_id, parameter) FROM stdin;
\.


--
-- Data for Name: test_lavinia_foods_in_foodsets_list; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_lavinia_foods_in_foodsets_list (set_id, set_name, food_id, food_name) FROM stdin;
\.


--
-- Data for Name: test_lavinia_foodset_list; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_lavinia_foodset_list (parent_set_id, parent_set_name, set_id, set_name, set_level) FROM stdin;
\.


--
-- Data for Name: test_lavinia_item_list; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_lavinia_item_list (id, type, short_name, long_name, unit_id, unit_name, unit_long_name) FROM stdin;
\.


--
-- Data for Name: test_lavinia_keszetel_list; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_lavinia_keszetel_list (parent_set_id, parent_set_name, set_id, set_name, set_level) FROM stdin;
\.


--
-- Data for Name: test_lavinia_nutrients_in_foodsets_list; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_lavinia_nutrients_in_foodsets_list (food_id, elelm_nev, source_id, nutr_id, tapanyag_nev, fc_quantity) FROM stdin;
\.


--
-- Data for Name: test_lavinia_recipe_contents_in_keszetel_list; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_lavinia_recipe_contents_in_keszetel_list (set_id, set_name, rec_id, recipe_name, food_id, food_name, rc_quantity, unit_id, operation_id, operation_name) FROM stdin;
\.


--
-- Data for Name: test_vi_food_cont_source_nevekkel; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY test_vi_food_cont_source_nevekkel (elem_id, elelm_nev, forras_id, forras_nev, tapanyag_id, tapanyag_nev, mennyiseg, mertegys_id, mertegys_nev) FROM stdin;
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY "user" (user_id, user_type_code, firstname, family_name, mobile, skype, email, google_account, ios_account, ds_id, lavinia_name, default_insulin_type_id, user_desc, illness_type_code) FROM stdin;
7017	1	john		\N	\N	john@gmail.com	john@gmail.com	\N	5293785087bb696e12140406	\N	\N		\N
7018	1	jane		\N	\N	jane@gmail.com	jane@gmail.com	\N	5293785187bb696e12140407	\N	\N		\N
7019	1	diet		\N	\N	diet@gmail.com	diet@gmail.com	\N	5293785187bb696e12140408	\N	\N		\N
7020	1	honved-B		\N	\N	honved-B	honved-B	\N	5293785187bb696e1214040a	\N	\N		\N
7021	1	honved-A		\N	\N	honved-A	honved-A	\N	5293785187bb696e12140409	\N	\N		\N
7022	1	honved-C		\N	\N	honved-C	honved-C	\N	5293785287bb696e1214040b	\N	\N		\N
7023	1	honved-CA		\N	\N	honved-CA	honved-CA	\N	5293785287bb696e1214040c	\N	\N		\N
7024	1	honved-CB		\N	\N	honved-CB	honved-CB	\N	5293785287bb696e1214040d	\N	\N		\N
7025	1	Cseh	Lajos Tamás	\N	\N	cselt89@gmail.com:archive-2014-12-02	cselt89@gmail.com:archive-2014-12-02	\N	5293785287bb696e1214040e	\N	\N	Archived: 2014-12-02 13:45\r\n---\r\n	\N
7026	1	Kozmann	György	\N	\N	kozmann@virt.uni-pannon.hu	kozmann@virt.uni-pannon.hu	\N	5293785387bb696e1214040f	\N	\N		\N
7027	1	lonely		\N	\N	lonely@gmail.com	lonely@gmail.com	\N	5293785387bb696e12140410	\N	\N		\N
7028	1	Kósa	István	\N	\N	kosaist@gmail.com	kosaist@gmail.com	\N	5293785387bb696e12140411	\N	\N		\N
7029	1	Vassányi	István	\N	\N	vassanyi@almos.vein.hu	vassanyi@almos.vein.hu	\N	5293785387bb696e12140412	\N	\N		\N
7030	1	Nemes	Márta	\N	\N	nemesmarta03@gmail.com	nemesmarta03@gmail.com	\N	5293785487bb696e12140413	\N	\N		\N
7031	1	Kloon		\N	\N	kloon@gmail.com	kloon@gmail.com	\N	5293785487bb696e12140414	\N	\N		\N
7032	1	predy		\N	\N	predy.hu@gmail.com:archive-2014-09-11	predy.hu@gmail.com:archive-2014-09-11	\N	5293785487bb696e12140416	\N	\N	Archived: 2014-09-11 17:09\r\n---\r\n	\N
7033	1	Kloó	Norbert	\N	\N	kloon15@gmail.com	kloon15@gmail.com	\N	5293785487bb696e12140415	\N	\N		\N
7034	1	Gaál	Balázs	\N	\N	bgaal@ginf.hu	bgaal@ginf.hu	\N	5293785587bb696e12140417	\N	\N		\N
7035	1	Pato	Sandor	\N	\N	sanyi2	sanyi2	\N	5293785587bb696e12140418	\N	\N		\N
7036	1	Gyuk	Péter	\N	\N	gyukpeti@gmail.com	gyukpeti@gmail.com	\N	5293785587bb696e12140419	\N	\N		\N
7037	1	system		\N	\N	system	system	\N	5293785587bb696e1214041a	\N	\N		\N
7038	1	kozmann		\N	\N	kozmann	kozmann	\N	5293785687bb696e1214041b	\N	\N		\N
7039	1	Tuba	Orsolya	\N	\N	orsii.1997@gmail.com	orsii.1997@gmail.com	\N	5293785687bb696e1214041c	\N	\N		\N
7040	1	testuser		\N	\N	test1@test.hu	test1@test.hu	\N	5293785687bb696e1214041d	\N	\N		\N
7041	1	Test	User	\N	\N	test@test.com	test@test.com	\N	5293785687bb696e1214041e	\N	\N		\N
7042	1	Pato	Sandor	\N	\N	pato.sandor@gmail.com	pato.sandor@gmail.com	\N	5293785787bb696e1214041f	\N	\N		\N
7043	1	demo1		\N	\N	demo1	demo1	\N	5293785787bb696e12140420	\N	\N		\N
7044	1	Illés	Tímea	\N	\N	illes.timea	illes.timea	\N	5293785787bb696e12140421	\N	\N		\N
7045	1	Miletics	Pál	\N	\N	miletics	miletics	\N	5293785787bb696e12140422	\N	\N		\N
7046	1	testuser		\N	\N	test@test.hu	test@test.hu	\N	5293785887bb696e12140423	\N	\N		\N
7047	1	Szabó	László	\N	\N	lezli01@gmail.com	lezli01@gmail.com	\N	5293785887bb696e12140424	\N	\N		\N
7048	1	Opra	Barna	\N	\N	barna.opra@gmx.net	barna.opra@gmx.net	\N	5293785887bb696e12140425	\N	\N		\N
7049	1	Aaa	Bbb	\N	\N	a.b@c.d	a.b@c.d	\N	5293785887bb696e12140426	\N	\N		\N
7050	1	Tóth	Tamás	\N	\N	ttamas85@gmail.com	ttamas85@gmail.com	\N	5293785987bb696e12140427	\N	\N		\N
7051	1	ttest1		\N	\N	ttest1	ttest1	\N	5293785987bb696e12140428	\N	\N		\N
7052	1	ttest2		\N	\N	ttest2	ttest2	\N	5293785987bb696e12140429	\N	\N		\N
7053	1	ttest3		\N	\N	ttest3	ttest3	\N	5293785987bb696e1214042a	\N	\N		\N
7054	1	Endrényi	Tamás	\N	\N	tamas.endrenyi@roche.com	tamas.endrenyi@roche.com	\N	5293785a87bb696e1214042b	\N	\N		\N
7055	1	demo	user	\N	\N	demo123@demo.com	demo123@demo.com	\N	5293785a87bb696e1214042c	\N	\N		\N
7056	1	Opra	Barna	\N	\N	barna.opra	barna.opra	\N	5293785a87bb696e1214042d	\N	\N		\N
7057	1	Endrényi	Tamás	\N	\N	tamas.endrenyi	tamas.endrenyi	\N	5293785a87bb696e1214042e	\N	\N		\N
7058	1	Czene		\N	\N	czene	czene	\N	5293785b87bb696e1214042f	\N	\N		\N
7059	1	ttest4		\N	\N	ttest4	ttest4	\N	5293785b87bb696e12140430	\N	\N		\N
7060	1	ttest5		\N	\N	ttest5	ttest5	\N	5293785b87bb696e12140431	\N	\N		\N
7061	1	ttest7		\N	\N	ttest7	ttest7	\N	5293785c87bb696e12140433	\N	\N		\N
7062	1	ttest6		\N	\N	ttest6	ttest6	\N	5293785c87bb696e12140432	\N	\N		\N
7063	1	ttest8		\N	\N	ttest8	ttest8	\N	5293785c87bb696e12140434	\N	\N		\N
7064	1	ttest9		\N	\N	ttest9	ttest9	\N	5293785c87bb696e12140435	\N	\N		\N
7065	1	diet		\N	\N	diet@diet	diet@diet	\N	52a5a20487bb49147b509d10	\N	\N		\N
7066	1	doctor		\N	\N	doctor@doctor	doctor@doctor	\N	52a5a20487bb49147b509d11	\N	\N		\N
7067	1	menugene.test.1@gmail.com		\N	\N	menugene.test.1@gmail.com	menugene.test.1@gmail.com	\N	52cd82eee4b0b0b931efaf81	\N	\N		\N
7068	1	Fodor	András	\N	\N	fodorandras1963@gmail.com	fodorandras1963@gmail.com	\N	52d682dc6329be5bed265d2c	\N	\N		\N
7069	1	admin		\N	\N	admin@admin	admin@admin	\N	529de3bc8fdf0a8ec3091496	\N	\N		\N
7070	1	Honved	Lavinia	\N	\N	test.lavinia.1@gmail.com:archive-2014-09-24	test.lavinia.1@gmail.com:archive-2014-09-24	\N	52d682dc6329be5bed265d2d	\N	\N	Archived: 2014-09-24 14:27\r\n---\r\n"Angol beteg"	\N
7071	1	Honved	Lavinia	\N	\N	test.lavinia.2@gmail.com:archive-2014-10-10	test.lavinia.2@gmail.com:archive-2014-10-10	\N	52d682dd6329be5bed265d2e	\N	\N	Archived: 2014-10-10 14:11\r\n---\r\n1162V\tViszk....\t1950.09.30.\t64\tffi\tP1\tNexus 7 Tablet\ttest.lavinia.2@gmail.com\tL4v1n14.2\t2014.09.17.\t2014.10.06.\t\t-\t2014.10.10.\t2014.10.10.\t\t\t356619-05-194322-0\tKósa SZTE\t	\N
7072	1	Honved	Lavinia	\N	\N	test.lavinia.3@gmail.com:archive-2014-09-16	test.lavinia.3@gmail.com:archive-2014-09-16	\N	52d682dd6329be5bed265d2f	\N	\N	Archived: 2014-09-16 14:52\r\n---\r\n	\N
7073	1	Honved	Lavinia	\N	\N	test.lavinia.4@gmail.com:archive-2014-10-27	test.lavinia.4@gmail.com:archive-2014-10-27	\N	52d682dd6329be5bed265d30	\N	\N	Archived: 2014-10-27 14:14\r\n---\r\n2151OA\tObermayer András\t1947.03.22.\t67\tffi\tP4\t\ttest.lavinia.4@gmail.com\t\t2014.10.02.\t0214.10.27.\t\t\t0214.10.27.	\N
7074	1	Honved	Lavinia	\N	\N	test.lavinia.5@gmail.com:archive-2014-10-10	test.lavinia.5@gmail.com:archive-2014-10-10	\N	52d682dd6329be5bed265d31	\N	\N	Archived: 2014-10-10 14:12\r\n---\r\n2162GP\tGallai Péter\t1951.11.22.\t63\tffi\tP3\t\ttest.lavinia.5@gmail.com\t\t2014.09.19.\t2014.10.09.\tpulzus felhasználó saját google drive fiókjába fel lett töltve (Informatikus a paciens) \t-\t2014.10.10.\t2014.10.10.	\N
7075	1	Fecco		\N	\N	fecco001@gmail.com	fecco001@gmail.com	\N	52d682de6329be5bed265d32	\N	\N		\N
7076	1	mario.salai@gmail.com		\N	\N	mario.salai@gmail.com	mario.salai@gmail.com	\N	52ed5874e4b09a5f6ed282ce	\N	\N		\N
7077	1	Vassányi	István	\N	\N	ivassany@gmail.com	ivassany@gmail.com	\N	52f0ed27db2d989699302d08	\N	\N		\N
7078	1	Honved	Lavinia Doctor	\N	\N	test.lavinia.doctor@gmail.com	test.lavinia.doctor@gmail.com	\N	52f37af3e4b09a5f6ed287b1	\N	\N		\N
7079	1	menugene.test.3@gmail.com		\N	\N	menugene.test.3@gmail.com	menugene.test.3@gmail.com	\N	5303365de4b037ed6f9676e7	\N	\N		\N
7080	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com:archive-2014-10-10	test.lavinia.6@gmail.com:archive-2014-10-10	\N	5304bc5de4b0010e07ba0993	\N	\N	Archived: 2014-10-10 14:12\r\n---\r\n2161TJ\tTokai János\t1949.11.26.\t65\tffi\tP2\t\ttest.lavinia.6@gmail.com\t\t2014.09.19.\t2014.10.08.\t\t-\t2014.10.10.\t2014.10.10.	\N
7081	1	test.lavinia.7@gmail.com		\N	\N	test.lavinia.7@gmail.com:archive-2014-10-21	test.lavinia.7@gmail.com:archive-2014-10-21	\N	530611c4e4b02bd83c8a791d	\N	\N	Archived: 2014-10-21 11:34\r\n---\r\n	\N
7082	1	kavaleczmate@gmail.com		\N	\N	kavaleczmate@gmail.com	kavaleczmate@gmail.com	\N	530dae33e4b062e162c35b02	\N	\N		\N
7083	1	menugene.watch@gmail.com		\N	\N	menugene.watch@gmail.com	menugene.watch@gmail.com	\N	530e007ce4b09d6571d82879	\N	\N		\N
7084	1	tothmajorgizabella@gmail.com		\N	\N	tothmajorgizabella@gmail.com	tothmajorgizabella@gmail.com	\N	531dbbdee4b0ece504e09497	\N	\N		\N
7085	1	test.lavinia.8@gmail.com		\N	\N	test.lavinia.8@gmail.com:archive-2014-10-29	test.lavinia.8@gmail.com:archive-2014-10-29	\N	531f070ee4b0ece504e09518	\N	\N	Archived: 2014-10-29 15:43\r\n---\r\n	\N
7086	1	test.lavinia.9@gmail.com		\N	\N	test.lavinia.9@gmail.com:archive-2014-10-29	test.lavinia.9@gmail.com:archive-2014-10-29	\N	531f0c91e4b0ece504e0951a	\N	\N	Archived: 2014-10-29 14:46\r\n---\r\n	\N
7087	1	test.lavinia.10@gmail.com		\N	\N	test.lavinia.10@gmail.com:archive-2014-12-08	test.lavinia.10@gmail.com:archive-2014-12-08	\N	531f2577e4b054238b56d5cd	\N	\N	Archived: 2014-12-08 12:57\r\n---\r\n	\N
7088	1	blazefirst@gmail.com		\N	\N	blazefirst@gmail.com	blazefirst@gmail.com	\N	532057cee4b054238b56d631	\N	\N		\N
7089	1	test.lavinia.11@gmail.com		\N	\N	test.lavinia.11@gmail.com:archive-2014-10-29	test.lavinia.11@gmail.com:archive-2014-10-29	\N	53205b9ee4b054238b56d646	\N	\N	Archived: 2014-10-29 14:37\r\n---\r\n	\N
7090	1	ferencpongracz139@gmail.com		\N	\N	ferencpongracz139@gmail.com	ferencpongracz139@gmail.com	\N	5320618ce4b054238b56d650	\N	\N		\N
7091	1	domerobi2@gmail.com		\N	\N	domerobi2@gmail.com	domerobi2@gmail.com	\N	53248b38e4b0967311e5d554	\N	\N		\N
7092	1	judit.tapolca@gmail.com		\N	\N	judit.tapolca@gmail.com	judit.tapolca@gmail.com	\N	53411ed5e4b0967311e5f827	\N	\N		\N
7093	1	test.lavinia.12@gmail.com		\N	\N	test.lavinia.12@gmail.com:archive-2014-10-29	test.lavinia.12@gmail.com:archive-2014-10-29	\N	53427220e4b0967311e5fa7e	\N	\N	Archived: 2014-10-29 14:02\r\n---\r\n	\N
7094	1	test.lavinia.vandorfy@gmail.com		\N	\N	test.lavinia.vandorfy@gmail.com	test.lavinia.vandorfy@gmail.com	\N	5368fe23e4b0fb8237ea473c	\N	\N		\N
7095	1	test.lavinia.15@gmail.com		\N	\N	test.lavinia.15@gmail.com:archive-2014-10-29	test.lavinia.15@gmail.com:archive-2014-10-29	\N	53833831e4b0c587575f7eea	\N	\N	Archived: 2014-10-29 12:14\r\n---\r\n	\N
7096	1	test.lavinia.16@gmail.com		\N	\N	test.lavinia.16@gmail.com:archive-2014-10-14	test.lavinia.16@gmail.com:archive-2014-10-14	\N	539edb03e4b08f3d28a9c6be	\N	\N	Archived: 2014-10-14 14:32\r\n---\r\n	\N
7097	1	istok.bela52@gmail.com		\N	\N	istok.bela52@gmail.com	istok.bela52@gmail.com	\N	53aa9221e4b08cc9a11a02c9	\N	\N		\N
7098	1	test.lavinia.18@gmail.com		\N	\N	test.lavinia.18@gmail.com:archive-2014-10-14	test.lavinia.18@gmail.com:archive-2014-10-14	\N	53ba83d3e4b08cc9a11a0f61	\N	\N	Archived: 2014-10-14 13:58\r\n---\r\n	\N
7099	1	test.lavinia.14@gmail.com		\N	\N	test.lavinia.14@gmail.com:archive-2014-10-29	test.lavinia.14@gmail.com:archive-2014-10-29	\N	53831f2de4b0c587575f7e9e	\N	\N	Archived: 2014-10-29 13:05\r\n---\r\n	\N
7100	1	fkornel@gmail.com		\N	\N	fkornel@gmail.com	fkornel@gmail.com	\N	537a71e1e4b0c587575f79ca	\N	\N		\N
7101	1	test.lavinia.13@gmail.com		\N	\N	test.lavinia.13@gmail.com:archive-2014-10-29	test.lavinia.13@gmail.com:archive-2014-10-29	\N	5383353de4b0c587575f7ee8	\N	\N	Archived: 2014-10-29 13:19\r\n---\r\n	\N
7102	1	bgm.triad@gmail.com		\N	\N	bgm.triad@gmail.com	bgm.triad@gmail.com	\N	53bfb804e4b08cc9a11a1291	\N	\N		\N
7103	1	Zoltán	Szopory	\N	\N	zespamz@gmail.com	zespamz@gmail.com	\N	53bfd449e4b08cc9a11a12a7	\N	\N		\N
7104	1	test.lavinia.17@gmail.com		\N	\N	test.lavinia.17@gmail.com:archive-2014-10-14	test.lavinia.17@gmail.com:archive-2014-10-14	\N	53c003c6e4b08cc9a11a12b8	\N	\N	Archived: 2014-10-14 14:02\r\n---\r\n	\N
7105	1	test.lavinia.19@gmail.com		\N	\N	test.lavinia.19@gmail.com:archive-2014-09-17	test.lavinia.19@gmail.com:archive-2014-09-17	\N	53c66efce4b08cc9a11a1941	\N	\N	Archived: 2014-09-17 13:51\r\n---\r\n	\N
7106	1	sir.csarli@gmail.com		\N	\N	sir.csarli@gmail.com	sir.csarli@gmail.com	\N	53c187bae4b08cc9a11a1375	\N	\N		\N
7107	1	test.lavinia.21@gmail.com		\N	\N	test.lavinia.21@gmail.com:archive-2014-09-24	test.lavinia.21@gmail.com:archive-2014-09-24	\N	53c66fb8e4b08cc9a11a1944	\N	\N	Archived: 2014-09-24 13:06\r\n---\r\n	\N
7108	1	test.lavinia.22@gmail.com		\N	\N	test.lavinia.22@gmail.com:archive-2014-09-16	test.lavinia.22@gmail.com:archive-2014-09-16	\N	53c675aee4b08cc9a11a1b74	\N	\N	Archived: 2014-09-16 13:59\r\n---\r\n	\N
7109	1	mategyomrei@gmail.com		\N	\N	mategyomrei@gmail.com	mategyomrei@gmail.com	\N	53d7a809e4b08cc9a11a3614	\N	\N		\N
7110	1	komila20121205@gmail.com		\N	\N	komila20121205@gmail.com	komila20121205@gmail.com	\N	53cd0aaae4b08cc9a11a2e5d	\N	\N		\N
7111	1	diaeuro.doctor@gmail.com		\N	\N	diaeuro.doctor@gmail.com	diaeuro.doctor@gmail.com	\N	53e1f808e4b08cc9a11a3d8e	\N	\N		\N
7112	1	tomikapc@gmail.com		\N	\N	tomikapc@gmail.com	tomikapc@gmail.com	\N	53e3d6c4e4b08cc9a11a3f98	\N	\N		\N
7113	1	czboy72@gmail.com		\N	\N	czboy72@gmail.com	czboy72@gmail.com	\N	53e4fd9de4b08cc9a11a4385	\N	\N		\N
7114	1	lorinczkiikki@gmail.com		\N	\N	lorinczkiikki@gmail.com	lorinczkiikki@gmail.com	\N	53f4638be4b0d810af1bf22d	\N	\N		\N
7115	1	test.lavinia.20@gmail.com		\N	\N	test.lavinia.20@gmail.com:archive-2014-09-16	test.lavinia.20@gmail.com:archive-2014-09-16	\N	53fdd4b8e4b0d810af1c0bb9	\N	\N	Archived: 2014-09-16 16:26\r\n---\r\n	\N
7116	1	test.lavinia.25@gmail.com		\N	\N	test.lavinia.25@gmail.com	test.lavinia.25@gmail.com	\N	54058e5fe4b0d810af1c12f3	\N	\N		\N
7117	1	test.lavinia.24@gmail.com		\N	\N	test.lavinia.24@gmail.com	test.lavinia.24@gmail.com	\N	54007598e4b0d810af1c0db2	\N	\N		\N
7118	1	test.lavinia.26@gmail.com		\N	\N	test.lavinia.26@gmail.com:archive-2015-01-22	test.lavinia.26@gmail.com:archive-2015-01-22	\N	5405919ce4b0d810af1c12f6	\N	\N	Archived: 2015-01-22 13:55\r\n---\r\nVanderlich	\N
7119	1	test.lavinia.23@gmail.com		\N	\N	test.lavinia.23@gmail.com:archive-2014-09-17	test.lavinia.23@gmail.com:archive-2014-09-17	\N	5405a066e4b0d810af1c134e	\N	\N	Archived: 2014-09-17 15:05\r\n---\r\n	\N
7120	1	zsaki.erik@gmail.com		\N	\N	zsaki.erik@gmail.com	zsaki.erik@gmail.com	\N	540d7771e4b0d810af1c2763	\N	\N		\N
7121	1	lenyi.szilvia@gmail.com		\N	\N	lenyi.szilvia@gmail.com	lenyi.szilvia@gmail.com	\N	5410a706e4b0d810af1c314b	\N	\N		\N
7122	1	kopatga@gmail.com		\N	\N	kopatga@gmail.com	kopatga@gmail.com	\N	5416a5b4e4b03f6d804dc930	\N	\N		\N
7123	1	predy.hu@gmail.com		\N	\N	predy.hu@gmail.com	predy.hu@gmail.com	\N	5411bb5ae4b03f6d804dc4e0	\N	\N		\N
7124	1	test.lavinia.22@gmail.com		\N	\N	test.lavinia.22@gmail.com:archive-2015-02-04	test.lavinia.22@gmail.com:archive-2015-02-04	\N	54182936e4b03f6d804dd25f	\N	\N	Archived: 2015-02-04 14:39\r\n---\r\nVanderlich	\N
7125	1	test.lavinia.3@gmail.com		\N	\N	test.lavinia.3@gmail.com:archive-2015-01-14	test.lavinia.3@gmail.com:archive-2015-01-14	\N	5418329ae4b03f6d804dd37c	\N	\N	Archived: 2015-01-14 16:09\r\n---\r\nVanderlich	\N
7126	1	test.lavinia.20@gmail.com		\N	\N	test.lavinia.20@gmail.com:archive-2015-01-14	test.lavinia.20@gmail.com:archive-2015-01-14	\N	541848c1e4b03f6d804dd3a3	\N	\N	Archived: 2015-01-14 16:15\r\n---\r\nVanderlich	\N
7127	1	test.lavinia.23@gmail.com		\N	\N	test.lavinia.23@gmail.com	test.lavinia.23@gmail.com	\N	54198708e4b0b72c90600873	\N	\N		\N
7128	1	test.lavinia.19@gmail.com		\N	\N	test.lavinia.19@gmail.com:archive-2014-12-18	test.lavinia.19@gmail.com:archive-2014-12-18	\N	541977bce4b0b72c9060071b	\N	\N	Archived: 2014-12-18 14:44\r\n---\r\nHK-3 kísérlet	\N
7129	1	galdaz.biz@gmail.com		\N	\N	galdaz.biz@gmail.com	galdaz.biz@gmail.com	\N	541b4e6ce4b0b72c906018a8	\N	\N		\N
7130	1	takacsgyula60@gmail.com		\N	\N	takacsgyula60@gmail.com	takacsgyula60@gmail.com	\N	541d5a54e4b0b72c90601a0f	\N	\N		\N
7131	1	test.lavinia.21@gmail.com		\N	\N	test.lavinia.21@gmail.com:archive-2014-10-30	test.lavinia.21@gmail.com:archive-2014-10-30	\N	5422b1dce4b0a769abd2b2cc	\N	\N	Archived: 2014-10-30 15:32\r\n---\r\nPintér Bernadett	\N
7132	1	iannus62@gmail.com		\N	\N	iannus62@gmail.com	iannus62@gmail.com	\N	543b7153e4b0a769abd2eaf9	\N	\N		\N
7133	1	balintka77@gmail.com		\N	\N	balintka77@gmail.com	balintka77@gmail.com	\N	542bed48e4b0a769abd2d171	\N	\N		\N
7134	1	diaeuro	doctor	\N	\N	diaeuro@doctor	diaeuro@doctor	\N	543b8855e4b0a769abd2eb08	\N	\N		\N
7135	1	test.lavinia.18@gmail.com		\N	\N	test.lavinia.18@gmail.com	test.lavinia.18@gmail.com	\N	543d100ee4b0bcf96752dbdd	\N	\N		\N
7136	1	test.lavinia.17@gmail.com		\N	\N	test.lavinia.17@gmail.com	test.lavinia.17@gmail.com	\N	543d10f2e4b0bcf96752dbe2	\N	\N		\N
7137	1	test.lavinia.16@gmail.com		\N	\N	test.lavinia.16@gmail.com	test.lavinia.16@gmail.com	\N	543d17f8e4b0bcf96752dc00	\N	\N		\N
7138	1	diaeuro@doctor.hu		\N	\N	diaeuro@doctor.hu	diaeuro@doctor.hu	\N	543d1a3ce4b0bcf96752dc03	\N	\N		\N
7139	1	szalka.brigitta@gmail.com		\N	\N	szalka.brigitta@gmail.com	szalka.brigitta@gmail.com	\N	543e3026e4b0bcf96752e65f	\N	\N		\N
7140	1	test.lavinia.7@gmail.com		\N	\N	test.lavinia.7@gmail.com	test.lavinia.7@gmail.com	\N	544628bae4b0a2324bf7509f	\N	\N		\N
7141	1	test.lavinia.2@gmail.com		\N	\N	test.lavinia.2@gmail.com:archive-2014-10-27	test.lavinia.2@gmail.com:archive-2014-10-27	\N	5447c81ee4b02a63f2fa2ad1	\N	\N	Archived: 2014-10-27 14:13\r\n---\r\n2016RF\tRaidl Ferenc\t1953.04.04.\t61\tffi\tNovacord\tNexus 7 Tablet\ttest.lavinia.2@gmail.com\t\t2014.10.20.\t2014.10.27.\t\t\t0214.10.27.\r\n	\N
7142	1	test.lavinia.1@gmail.com		\N	\N	test.lavinia.1@gmail.com	test.lavinia.1@gmail.com	\N	544a1e5ae4b02a63f2fa2c59	\N	\N		\N
7143	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com:archive-2014-11-27	test.lavinia.6@gmail.com:archive-2014-11-27	\N	544e5101e4b02a63f2fa2fdf	\N	\N	Archived: 2014-11-27 15:37\r\n---\r\nHK2 P8	\N
7144	1	test.lavinia.5@gmail.com		\N	\N	test.lavinia.5@gmail.com:archive-2014-11-27	test.lavinia.5@gmail.com:archive-2014-11-27	\N	544e510de4b02a63f2fa2fe5	\N	\N	Archived: 2014-11-27 15:35\r\n---\r\nHK2 p7\r\n	\N
7145	1	test.lavinia.4@gmail.com		\N	\N	test.lavinia.4@gmail.com:archive-2014-11-28	test.lavinia.4@gmail.com:archive-2014-11-28	\N	544e5146e4b02a63f2fa2fed	\N	\N	Archived: 2014-11-28 11:44\r\n---\r\nHK2 P5	\N
7146	1	test.lavinia.2@gmail.com		\N	\N	test.lavinia.2@gmail.com:archive-2014-11-28	test.lavinia.2@gmail.com:archive-2014-11-28	\N	544e512de4b02a63f2fa2feb	\N	\N	Archived: 2014-11-28 11:41\r\n---\r\nHK2 P6	\N
7147	1	test.lavinia.15@gmail.com		\N	\N	test.lavinia.15@gmail.com:archive-2015-02-24	test.lavinia.15@gmail.com:archive-2015-02-24	\N	5450cf6be4b0102966264fe9	\N	\N	Archived: 2015-02-24 14:35\r\n---\r\nTapolca CGMS1	\N
7148	1	test.lavinia.14@gmail.com		\N	\N	test.lavinia.14@gmail.com	test.lavinia.14@gmail.com	\N	5450d81ee4b010296626503c	\N	\N		\N
7149	1	test.lavinia.13@gmail.com		\N	\N	test.lavinia.13@gmail.com	test.lavinia.13@gmail.com	\N	5450dd77e4b010296626504f	\N	\N		\N
7150	1	test.lavinia.12@gmail.com		\N	\N	test.lavinia.12@gmail.com	test.lavinia.12@gmail.com	\N	5450e9f0e4b010296626506c	\N	\N		\N
7151	1	test.lavinia.11@gmail.com		\N	\N	test.lavinia.11@gmail.com	test.lavinia.11@gmail.com	\N	5450ede1e4b0102966265088	\N	\N		\N
7152	1	test.lavinia.9@gmail.com		\N	\N	test.lavinia.9@gmail.com	test.lavinia.9@gmail.com	\N	5450f652e4b01029662650b7	\N	\N		\N
7153	1	test.lavinia.21@gmail.com		\N	\N	test.lavinia.21@gmail.com	test.lavinia.21@gmail.com	\N	54524bf0e4b01029662662db	\N	\N	Galló Alexandra, Vanderlich	\N
7154	1	test.lavinia.8@gmail.com		\N	\N	test.lavinia.8@gmail.com:archive-2014-12-09	test.lavinia.8@gmail.com:archive-2014-12-09	\N	5450fd0ee4b01029662650da	\N	\N	Archived: 2014-12-09 14:50\r\n---\r\n	\N
7155	1	hajni.vida@gmail.com		\N	\N	hajni.vida@gmail.com	hajni.vida@gmail.com	\N	54577642e4b0102966266ad0	\N	\N		\N
7156	1	test.lavinia.27@gmail.com		\N	\N	test.lavinia.27@gmail.com:archive-2014-12-16	test.lavinia.27@gmail.com:archive-2014-12-16	\N	5460bb7fe4b01029662699c1	\N	\N	Archived: 2014-12-16 14:51\r\n---\r\nMario kísérlet\r\n	\N
7157	1	profnagyzoltan@gmail.com		\N	\N	profnagyzoltan@gmail.com	profnagyzoltan@gmail.com	\N	5464ae55e4b010296626ad6f	\N	\N		\N
7158	1	rebazah@gmail.com		\N	\N	rebazah@gmail.com	rebazah@gmail.com	\N	54732398e4b010296626ca1a	\N	\N		\N
7159	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com:archive-2014-12-14	test.lavinia.6@gmail.com:archive-2014-12-14	\N	5477373ae4b010296626dba9	\N	\N	Archived: 2014-12-14 12:40\r\n---\r\n2061PF\tPató Ferenc\t1951.03.30.\t\tffi\tP9\t\ttest.lavinia.6@gmail.com\t\t2014.11.27.\t2014.12.14.	\N
7160	1	test.lavinia.5@gmail.com		\N	\N	test.lavinia.5@gmail.com:archive-2014-12-15	test.lavinia.5@gmail.com:archive-2014-12-15	\N	5477373fe4b010296626dbab	\N	\N	Archived: 2014-12-15 14:55\r\n---\r\n211TA\tTihanyi Alajos\t1943.05.14.\t\tffi\tP10\t\ttest.lavinia.5@gmail.com\t\t2014.11.27.\t2014.12.15.\t\t\t2014.12.15.\r\n	\N
7161	1	Cseh	Lajos Tamás	\N	\N	cselt89@gmail.com	cselt89@gmail.com	\N	547db55fe4b0fd1846deaaa2	\N	\N		\N
7162	1	gallaipeter51@gmail.com		\N	\N	gallaipeter51@gmail.com	gallaipeter51@gmail.com	\N	5478567fe4b010296626f8c9	\N	\N		\N
7163	1	test.lavinia.2@gmail.com		\N	\N	test.lavinia.2@gmail.com:archive-2014-12-26	test.lavinia.2@gmail.com:archive-2014-12-26	\N	548183fee4b0fd1846deb377	\N	\N	Archived: 2014-12-26 14:31\r\n---\r\n2151SK\t\r\n1939.01.31.\t\tnő\tP11\t\ttest.lavinia.2@gmail.com\t\t2014.12.05.\t2014.12.25.\t\t\t2014.12.26.	\N
7164	1	test.lavinia.28@gmail.com		\N	\N	test.lavinia.28@gmail.com	test.lavinia.28@gmail.com	\N	5485a732e4b0fd1846deb85b	\N	\N		\N
7165	1	test.lavinia.29@gmail.com		\N	\N	test.lavinia.29@gmail.com	test.lavinia.29@gmail.com	\N	5485ad13e4b0fd1846deb865	\N	\N		\N
7166	1	pato.ferenc3@gmail.com		\N	\N	pato.ferenc3@gmail.com	pato.ferenc3@gmail.com	\N	5485af6ce4b0fd1846deb868	\N	\N		\N
7167	1	test.lavinia.4@gmail.com		\N	\N	test.lavinia.4@gmail.com:archive-2014-12-26	test.lavinia.4@gmail.com:archive-2014-12-26	\N	5485aa2de4b0fd1846deb85e	\N	\N	Archived: 2014-12-26 14:32\r\n---\r\n2182MF\t\r\n1947.04.16.\t\tffi\tP12\t\ttest.lavinia.4@gmail.com\t\t2014.12.08.\t2014.12.26.\t\t\t2014.12.26.	\N
7168	1	test.lavinia.30@gmail.com		\N	\N	test.lavinia.30@gmail.com	test.lavinia.30@gmail.com	\N	5486d426e4b0fd1846deb976	\N	\N		\N
7169	1	test.lavinia.32@gmail.com		\N	\N	test.lavinia.32@gmail.com	test.lavinia.32@gmail.com	\N	5486ea7fe4b0fd1846deb9a9	\N	\N		\N
7170	1	test.lavinia.31@gmail.com		\N	\N	test.lavinia.31@gmail.com	test.lavinia.31@gmail.com	\N	5486eadfe4b0fd1846deb9ab	\N	\N		\N
7171	1	test.lavinia.8@gmail.com		\N	\N	test.lavinia.8@gmail.com:archive-2015-02-16	test.lavinia.8@gmail.com:archive-2015-02-16	\N	5486fe20e4b0fd1846deb9f3	\N	\N	Archived: 2015-02-16 10:08\r\n---\r\nVanderlich	\N
7172	1	mekisferenc1947@gmail.com		\N	\N	mekisferenc1947@gmail.com	mekisferenc1947@gmail.com	\N	548d86f1e4b0fd1846deefdf	\N	\N		\N
7173	1	gabi.szilvasy@gmail.com		\N	\N	gabi.szilvasy@gmail.com	gabi.szilvasy@gmail.com	\N	548dcc93e4b0fd1846def01a	\N	\N		\N
7174	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com:archive-2014-12-18	test.lavinia.6@gmail.com:archive-2014-12-18	\N	548ee98ce4b0fd1846def14f	\N	\N	Archived: 2014-12-18 07:53\r\n---\r\n3031HJ\tHász János\t\t\tffi\tP13\t\ttest.lavinia.6@gmail.com\t\t2014.12.15.\t2014.12.17.	\N
7175	1	Tapolca		\N	\N	tapolca@doctor	tapolca@doctor	\N	549034f9e4b0fd1846df1224	\N	\N		\N
7176	1	test.lavinia.27@gmail.com		\N	\N	test.lavinia.27@gmail.com:archive-2015-01-14	test.lavinia.27@gmail.com:archive-2015-01-14	\N	549038e6e4b0fd1846df1242	\N	\N	Archived: 2015-01-14 15:36\r\n---\r\n2131KZ\tKardos Zsigmondné\t\t1923.07.31.\t91\tnő\tP14\tNexus 5\ttest.lavinia.27@gmail.com\t\t2014.12.18.\t2014.12.18.\t\t\t???\t\t0	\N
7177	1	test.lavinia.5@gmail.com		\N	\N	test.lavinia.5@gmail.com:archive-2015-01-14	test.lavinia.5@gmail.com:archive-2015-01-14	\N	54927aebe4b0fd1846df154c	\N	\N	Archived: 2015-01-14 15:03\r\n---\r\n2132MF\tMartin Ferenc\t\t1949.11.08.\t65\tffi\tP15\tNexus 7\ttest.lavinia.5@gmail.com\t\t2014.12.18.\t2014.12.31.\t\t\t2015.01.05.\t\t13	\N
7178	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com:archive-2015-01-05	test.lavinia.6@gmail.com:archive-2015-01-05	\N	54927affe4b0fd1846df154e	\N	\N	Archived: 2015-01-05 15:24\r\n---\r\n1021BJ\tBíró János\t1946.10.11.\t68\tffi\tP17\t\ttest.lavinia.6@gmail.com\t\t2014.12.18.	\N
7179	1	test.lavinia.19@gmail.com		\N	\N	test.lavinia.19@gmail.com:archive-2015-01-05	test.lavinia.19@gmail.com:archive-2015-01-05	\N	5492dbdce4b0fd1846df1642	\N	\N	Archived: 2015-01-05 15:25\r\n---\r\n1191AG\tAlbrecht Gyula\t1959.08.20.\t55\tffi\tP18\t\ttest.lavinia.19@gmail.com\t\t2014.12.18.	\N
7180	1	test.lavinia.4@gmail.com		\N	\N	test.lavinia.4@gmail.com:archive-2015-01-05	test.lavinia.4@gmail.com:archive-2015-01-05	\N	549d81ebe4b0d069b193e65f	\N	\N	Archived: 2015-01-05 15:24\r\n---\r\n2012VT\tVarga Tibor\t1942.06.11.\t72\tffi\tP19\t\ttest.lavinia.4@gmail.com\t\t2014.12.26.	\N
7181	1	albrechtek@gmail.com		\N	\N	albrechtek@gmail.com	albrechtek@gmail.com	\N	54ac44f3e4b0d069b19416c2	\N	\N		\N
7182	1	test.lavinia.2@gmail.com		\N	\N	test.lavinia.2@gmail.com:archive-2015-01-23	test.lavinia.2@gmail.com:archive-2015-01-23	\N	54ad22c5e4b0d069b1941737	\N	\N	Archived: 2015-01-23 13:12\r\n---\r\nHK4 P01\r\nNI2111\tNovák Imre\t\t1952.07.21.\t62\t85\t165\t2-es típus (180g CH)\tffi\tP01\tNexus 7\ttest.lavinia.2@gmail.com\tleánya Novák Eszter\t\t2015.01.08.\t2015.01.15.\t2015.01.20.\t2015.01.23.\t121872\t2015.01.29.\t2015.02.05.\t2015.02.10.	\N
7183	1	test.lavinia.2@gmail.com		\N	\N	test.lavinia.2@gmail.com:archive-2015-01-05	test.lavinia.2@gmail.com:archive-2015-01-05	\N	549d8426e4b0d069b193e678	\N	\N	Archived: 2015-01-05 15:51\r\n---\r\n1091FL\tFarkas Lajos\t1938.06.18.\t76\tffi\tP20\t\ttest.lavinia.2@gmail.com\t\t2014.12.26.\t2014.01.05.\t\t\t2014.01.05.	\N
7184	1	test.lavinia.19@gmail.com		\N	\N	test.lavinia.19@gmail.com	test.lavinia.19@gmail.com	\N	54b6733ce4b0b2a38498feec	\N	\N		\N
7185	1	menugene.kozmann@gmail.com		\N	\N	menugene.kozmann@gmail.com	menugene.kozmann@gmail.com	\N	54b67266e4b0b2a38498fed4	\N	\N		\N
7186	1	test.lavinia.4@gmail.com		\N	\N	test.lavinia.4@gmail.com	test.lavinia.4@gmail.com	\N	54b682c2e4b0b2a38498ff97	\N	\N		\N
7187	1	test.lavinia.5@gmail.com		\N	\N	test.lavinia.5@gmail.com:archive-2015-02-10	test.lavinia.5@gmail.com:archive-2015-02-10	\N	54b67773e4b0b2a38498fef6	\N	\N	Archived: 2015-02-10 16:41\r\n---\r\nKF2132\tKrigler Ferenc\t\t\t1948.04.19.\t66\t94,9\t185\t2-es típus (180g CH)\tffi\tP04\tNexus 7\ttest.lavinia.5@gmail.com\tBrigi\t\t2015.01.19.\t\t\t2015.01.21.\t2015.02.03.\t2015.02.09.\t121872\t\t\t\t	\N
7188	1	test.lavinia.27@gmail.com		\N	\N	test.lavinia.27@gmail.com	test.lavinia.27@gmail.com	\N	54b684e2e4b0b2a38498ffa7	\N	\N		\N
7189	1	test.lavinia.3@gmail.com		\N	\N	test.lavinia.3@gmail.com	test.lavinia.3@gmail.com	\N	54b686e9e4b0b2a38498ffd9	\N	\N		\N
7190	1	test.lavinia.20@gmail.com		\N	\N	test.lavinia.20@gmail.com	test.lavinia.20@gmail.com	\N	54b6892be4b0b2a38498ffe7	\N	\N		\N
7191	1	test.lavinia.33@gmail.com		\N	\N	test.lavinia.33@gmail.com:archive-2015-02-06	test.lavinia.33@gmail.com:archive-2015-02-06	\N	54b9527be4b0b2a3849915c6	\N	\N	Archived: 2015-02-06 18:18\r\n---\r\nSS1232\tSzűcs Sándor\t\t\t1942.02.20.\t72\t96\t172\t2-es típus (180g CH)\tffi\tP02\tpapír\ttest.lavinia.33@gmail.com \tGyuk P. (Kész: 17 nap)\t2015.01.17.\t2015.01.12.\t\t\t2015.01.15.\t2015.01.20.\t2015.01.29.\t121930\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t	\N
7192	1	test.lavinia.26@gmail.com		\N	\N	test.lavinia.26@gmail.com	test.lavinia.26@gmail.com	\N	54c0f36de4b06966cb4f671f	\N	\N		\N
7193	1	test.lavinia.34@gmail.com		\N	\N	test.lavinia.34@gmail.com:archive-2015-02-06	test.lavinia.34@gmail.com:archive-2015-02-06	\N	54b9969ae4b0b2a3849917bd	\N	\N	Archived: 2015-02-06 15:55\r\n---\r\nCL2011\tCsima Lajos\t\t\t1936.06.08.\t78\t98,5\t176\t2-es típus (180g CH)\tffi\tP03\tpapír\ttest.lavinia.34@gmail.com\tLőrincz T.\t\t2015.01.14.\t\t\t2015.01.21.\t2015.01.28.\t2015.02.03.\t121930\t\t\t\t\tA beteg 26-ra hurutos lett (influenza??)\t\t\t\t\t\t\t\t\t\t	\N
7194	1	test.lavinia.35@gmail.com		\N	\N	test.lavinia.35@gmail.com	test.lavinia.35@gmail.com	\N	54ca59e5e4b0d94635f90f40	\N	\N		\N
7195	1	medimonitor20@gmail.com		\N	\N	medimonitor20@gmail.com	medimonitor20@gmail.com	\N	54c727a5e4b0d94635f8eb73	\N	\N		\N
7196	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com	test.lavinia.6@gmail.com	\N	54cb8f23e4b0d94635f917b6	\N	\N		\N
7197	1	aandrasj79@gmail.com		\N	\N	aandrasj79@gmail.com	aandrasj79@gmail.com	\N	54ccaa6ce4b0d94635f91855	\N	\N		\N
7198	1	kriglerferenc48@gmail.com		\N	\N	kriglerferenc48@gmail.com	kriglerferenc48@gmail.com	\N	54d1cabce4b04ccfb011d772	\N	\N		\N
7199	1	test.lavinia.22@gmail.com		\N	\N	test.lavinia.22@gmail.com	test.lavinia.22@gmail.com	\N	54d22119e4b04ccfb011dbb8	\N	\N		\N
7200	1	test.lavinia.2@gmail.com		\N	\N	test.lavinia.2@gmail.com	test.lavinia.2@gmail.com	\N	54d32e04e4b04ccfb011ec5b	\N	\N		\N
7201	1	test.lavinia.36@gmail.com		\N	\N	test.lavinia.36@gmail.com	test.lavinia.36@gmail.com	\N	54d21ff4e4b04ccfb011dbb5	\N	\N		\N
7202	1	test.lavinia.34@gmail.com		\N	\N	test.lavinia.34@gmail.com:archive-2015-03-04	test.lavinia.34@gmail.com:archive-2015-03-04	\N	54d4d647e4b04ccfb01213f0	\N	\N	Archived: 2015-03-04 13:24\r\n---\r\nBI2172\tBichacker Imre\t\t\t1956.03.22.\t?\t?\t?\t2-es típus (180g CH)\tffi\tP08\tpapír\ttest.lavinia.34@gmail.com\tLőrincz Tomi\t\t\tHétfő (3)\tVasárnap (3)\t2015.02.05.\t\t2015.02.14.\t121872\t\t\t\t\t2015.02.05-tól 24 órás HOLTER EKG\t\t\t\t\t\t\t\t\t\t\t	\N
7203	1	test.lavinia.33@gmail.com		\N	\N	test.lavinia.33@gmail.com	test.lavinia.33@gmail.com	\N	54d4f78ce4b0007344e85f3d	\N	\N		\N
7204	1	test.lavinia.5@gmail.com		\N	\N	test.lavinia.5@gmail.com	test.lavinia.5@gmail.com	\N	54dad11de4b02082ba34b63d	\N	\N		\N
7205	1	test.lavinia.8@gmail.com		\N	\N	test.lavinia.8@gmail.com	test.lavinia.8@gmail.com	\N	54e1b7c7e4b02082ba35192f	\N	\N		\N
7206	1	vassanyicsongor8@gmail.com		\N	\N	vassanyicsongor8@gmail.com	vassanyicsongor8@gmail.com	\N	54e617c8e4b02082ba35532c	\N	\N		\N
7207	1	test.lavinia.15@gmail.com		\N	\N	test.lavinia.15@gmail.com	test.lavinia.15@gmail.com	\N	54ed6628e4b02082ba3561c1	\N	\N		\N
7208	1	szuchyk@gmail.com		\N	\N	szuchyk@gmail.com	szuchyk@gmail.com	\N	54ef3e48e4b02082ba35919a	\N	\N		\N
7209	1	Vanderlich	Doctor	\N	\N	vanderlich@doctor	vanderlich@doctor	\N	54f6db9ee4b0b961568d7143	\N	\N		\N
7210	1	popuva@gmail.com		\N	\N	popuva@gmail.com	popuva@gmail.com	\N	549f0d73e4b0d069b193e860	\N	\N		\N
7211	1	test.lavinia.6@gmail.com		\N	\N	test.lavinia.6@gmail.com:archive-2015-01-22	test.lavinia.6@gmail.com:archive-2015-01-22	\N	54aa9f2ee4b0d069b19412eb	\N	\N	Archived: 2015-01-22 10:02\r\n---\r\nHK2 P22\r\n20182DI\tDudás Istvánné\t\t1944.06.03.\t70\tnő\tP22\tNexus 7\ttest.lavinia-6@gmail.com\t\t2015.01.05.	\N
7212	1	test.lavinia.34@gmail.com		\N	\N	test.lavinia.34@gmail.com	test.lavinia.34@gmail.com	\N	54f6f9c5e4b0b961568d8626	\N	\N		\N
\.


--
-- Data for Name: user_group; Type: TABLE DATA; Schema: log; Owner: postgres
--

COPY user_group (group_id, group_type_code, group_name, group_create_date, expert_id) FROM stdin;
\.


--
-- Name: user_group_group_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('user_group_group_id_seq', 1, false);


--
-- Name: user_user_id_seq; Type: SEQUENCE SET; Schema: log; Owner: postgres
--

SELECT pg_catalog.setval('user_user_id_seq', 7212, true);


--
-- Name: code_item_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY code_item
    ADD CONSTRAINT code_item_pkey PRIMARY KEY (type_id, item_id);


--
-- Name: code_type_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY code_type
    ADD CONSTRAINT code_type_pkey PRIMARY KEY (type_id);


--
-- Name: data_type_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY data_type
    ADD CONSTRAINT data_type_pkey PRIMARY KEY (type_id);


--
-- Name: device_data_type_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device_data_type
    ADD CONSTRAINT device_data_type_pkey PRIMARY KEY (model_id, type_id);


--
-- Name: device_model_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device_model
    ADD CONSTRAINT device_model_pkey PRIMARY KEY (model_id);


--
-- Name: device_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_pkey PRIMARY KEY (device_id);


--
-- Name: ep_event_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY ep_event
    ADD CONSTRAINT ep_event_pkey PRIMARY KEY (event_id);


--
-- Name: episode_device_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY episode_device
    ADD CONSTRAINT episode_device_pkey PRIMARY KEY (episode_id, device_id);


--
-- Name: episode_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY episode
    ADD CONSTRAINT episode_pkey PRIMARY KEY (episode_id);


--
-- Name: event_anamnesis_illness_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_anamnesis_illness
    ADD CONSTRAINT event_anamnesis_illness_pkey PRIMARY KEY (anamnesis_id, illness_id);


--
-- Name: event_anamnesis_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_anamnesis
    ADD CONSTRAINT event_anamnesis_pkey PRIMARY KEY (event_id);


--
-- Name: event_anamnesis_rda_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_anamnesis_rda
    ADD CONSTRAINT event_anamnesis_rda_pkey PRIMARY KEY (anamnesis_id, nutr_id);


--
-- Name: event_bp_meas_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_bp_meas
    ADD CONSTRAINT event_bp_meas_pkey PRIMARY KEY (event_id);


--
-- Name: event_comment_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_comment
    ADD CONSTRAINT event_comment_pkey PRIMARY KEY (event_id);


--
-- Name: event_glucose_meas_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_glucose_meas
    ADD CONSTRAINT event_glucose_meas_pkey PRIMARY KEY (event_id);


--
-- Name: event_item_content_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_item_content
    ADD CONSTRAINT event_item_content_pkey PRIMARY KEY (item_id, nutr_id);


--
-- Name: event_meal_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_meal
    ADD CONSTRAINT event_meal_pkey PRIMARY KEY (event_id);


--
-- Name: event_mealitem_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_mealitem
    ADD CONSTRAINT event_mealitem_pkey PRIMARY KEY (event_id);


--
-- Name: event_missing_food_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_missing_food
    ADD CONSTRAINT event_missing_food_pkey PRIMARY KEY (event_id);


--
-- Name: event_physical_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_physical
    ADD CONSTRAINT event_physical_pkey PRIMARY KEY (event_id);


--
-- Name: event_weight_meas_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_weight_meas
    ADD CONSTRAINT event_weight_meas_pkey PRIMARY KEY (event_id);


--
-- Name: expert_user_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY expert_user
    ADD CONSTRAINT expert_user_pkey PRIMARY KEY (user_id);


--
-- Name: group_member_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY group_member
    ADD CONSTRAINT group_member_pkey PRIMARY KEY (user_id, group_id, date_start);


--
-- Name: lavinia_illness_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY lavinia_illness
    ADD CONSTRAINT lavinia_illness_pkey PRIMARY KEY (illness_id);


--
-- Name: medication_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_medication
    ADD CONSTRAINT medication_pkey PRIMARY KEY (event_id);


--
-- Name: user_group_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY user_group
    ADD CONSTRAINT user_group_pkey PRIMARY KEY (group_id);


--
-- Name: user_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (user_id);


--
-- Name: code_item_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY code_item
    ADD CONSTRAINT code_item_fk FOREIGN KEY (type_id) REFERENCES code_type(type_id);


--
-- Name: device_data_type_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device_data_type
    ADD CONSTRAINT device_data_type_fk FOREIGN KEY (type_id) REFERENCES data_type(type_id);


--
-- Name: device_data_type_fk1; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device_data_type
    ADD CONSTRAINT device_data_type_fk1 FOREIGN KEY (model_id) REFERENCES device_model(model_id);


--
-- Name: device_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_fk FOREIGN KEY (model_id) REFERENCES device_model(model_id);


--
-- Name: ep_event_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY ep_event
    ADD CONSTRAINT ep_event_fk FOREIGN KEY (episode_id) REFERENCES episode(episode_id);


--
-- Name: ep_event_fk1; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY ep_event
    ADD CONSTRAINT ep_event_fk1 FOREIGN KEY (source_device_id) REFERENCES device(device_id);


--
-- Name: ep_event_fk2; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY ep_event
    ADD CONSTRAINT ep_event_fk2 FOREIGN KEY (meas_device_id) REFERENCES device(device_id);


--
-- Name: episode_device_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY episode_device
    ADD CONSTRAINT episode_device_fk FOREIGN KEY (episode_id) REFERENCES episode(episode_id);


--
-- Name: episode_device_fk1; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY episode_device
    ADD CONSTRAINT episode_device_fk1 FOREIGN KEY (device_id) REFERENCES device(device_id);


--
-- Name: episode_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY episode
    ADD CONSTRAINT episode_fk FOREIGN KEY (user_id) REFERENCES "user"(user_id);


--
-- Name: event_anamnesis_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_anamnesis
    ADD CONSTRAINT event_anamnesis_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_anamnesis_illness_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_anamnesis_illness
    ADD CONSTRAINT event_anamnesis_illness_fk FOREIGN KEY (anamnesis_id) REFERENCES event_anamnesis(event_id);


--
-- Name: event_anamnesis_rda_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_anamnesis_rda
    ADD CONSTRAINT event_anamnesis_rda_fk FOREIGN KEY (anamnesis_id) REFERENCES event_anamnesis(event_id);


--
-- Name: event_bp_meas_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_bp_meas
    ADD CONSTRAINT event_bp_meas_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_comment_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_comment
    ADD CONSTRAINT event_comment_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_glucose_meas_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_glucose_meas
    ADD CONSTRAINT event_glucose_meas_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_item_content_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_item_content
    ADD CONSTRAINT event_item_content_fk FOREIGN KEY (item_id) REFERENCES event_mealitem(event_id);


--
-- Name: event_meal_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_meal
    ADD CONSTRAINT event_meal_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_mealitem_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_mealitem
    ADD CONSTRAINT event_mealitem_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_mealitem_fk1; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_mealitem
    ADD CONSTRAINT event_mealitem_fk1 FOREIGN KEY (meal_id) REFERENCES event_meal(event_id);


--
-- Name: event_missing_food_fk2; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_missing_food
    ADD CONSTRAINT event_missing_food_fk2 FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_physical_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_physical
    ADD CONSTRAINT event_physical_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: event_weight_meas_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_weight_meas
    ADD CONSTRAINT event_weight_meas_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: expert_user_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY expert_user
    ADD CONSTRAINT expert_user_fk FOREIGN KEY (user_id) REFERENCES "user"(user_id);


--
-- Name: group_member_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY group_member
    ADD CONSTRAINT group_member_fk FOREIGN KEY (group_id) REFERENCES user_group(group_id);


--
-- Name: group_member_fk1; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY group_member
    ADD CONSTRAINT group_member_fk1 FOREIGN KEY (user_id) REFERENCES "user"(user_id);


--
-- Name: medication_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_medication
    ADD CONSTRAINT medication_fk FOREIGN KEY (event_id) REFERENCES ep_event(event_id);


--
-- Name: medication_fk3; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY event_medication
    ADD CONSTRAINT medication_fk3 FOREIGN KEY (related_meal_id) REFERENCES event_meal(event_id);


--
-- Name: user_group_fk; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY user_group
    ADD CONSTRAINT user_group_fk FOREIGN KEY (expert_id) REFERENCES expert_user(user_id);


--
-- Name: log; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA log FROM PUBLIC;
REVOKE ALL ON SCHEMA log FROM postgres;
GRANT ALL ON SCHEMA log TO postgres;


--
-- Name: add_group_member(integer, integer, date, date, character varying, character varying); Type: ACL; Schema: log; Owner: postgres
--

REVOKE ALL ON FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) FROM postgres;
GRANT ALL ON FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) TO postgres;
GRANT ALL ON FUNCTION add_group_member(var_user_id integer, var_group_id integer, var_date_start date, var_date_end date, var_internal_group_id character varying, var_external_group_id character varying) TO PUBLIC;


--
-- Name: add_user(smallint, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, integer, character varying, smallint, character varying); Type: ACL; Schema: log; Owner: postgres
--

REVOKE ALL ON FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying) FROM PUBLIC;
REVOKE ALL ON FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying) FROM postgres;
GRANT ALL ON FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying) TO postgres;
GRANT ALL ON FUNCTION add_user(var_user_type_code smallint, var_firstname character varying, var_family_name character varying, var_mobile character varying, var_skype character varying, var_email character varying, var_google_account character varying, var_ios_account character varying, var_ds_id character varying, var_lavinia_name character varying, var_default_insulin_type_id integer, var_user_desc character varying, var_illness_type_code smallint, var_expert_organisation character varying) TO PUBLIC;


--
-- Name: add_user_group(smallint, character varying, integer); Type: ACL; Schema: log; Owner: postgres
--

REVOKE ALL ON FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) FROM PUBLIC;
REVOKE ALL ON FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) FROM postgres;
GRANT ALL ON FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) TO postgres;
GRANT ALL ON FUNCTION add_user_group(var_group_type_code smallint, var_groupname character varying, var_expert_id integer) TO PUBLIC;


--
-- Name: start_episode(integer, smallint, date); Type: ACL; Schema: log; Owner: postgres
--

REVOKE ALL ON FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date) FROM PUBLIC;
REVOKE ALL ON FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date) FROM postgres;
GRANT ALL ON FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date) TO postgres;
GRANT ALL ON FUNCTION start_episode(var_user_id integer, var_ep_type_code smallint, var_start_date date) TO PUBLIC;


--
-- Name: test(integer, illness_list[]); Type: ACL; Schema: log; Owner: postgres
--

REVOKE ALL ON FUNCTION test(var_user_id integer, var_illness_list illness_list[]) FROM PUBLIC;
REVOKE ALL ON FUNCTION test(var_user_id integer, var_illness_list illness_list[]) FROM postgres;
GRANT ALL ON FUNCTION test(var_user_id integer, var_illness_list illness_list[]) TO postgres;
GRANT ALL ON FUNCTION test(var_user_id integer, var_illness_list illness_list[]) TO PUBLIC;


--
-- PostgreSQL database dump complete
--


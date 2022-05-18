package com.module.gomodules.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.module.gomodules.VO.CustomerVO;
import com.module.gomodules.VO.ReservationVO;
import com.module.gomodules.repository.UserRepository;
import com.module.gomodules.service.ReservationService;
//import com.module.gomodules.service.TableService;
import com.module.gomodules.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @ResponseBody // return to body
    @RequestMapping(value = "/joinUs.do", method = RequestMethod.POST)
    public String joinUs(HttpServletRequest req, CustomerVO vo) {
        vo.setVal_id(req.getParameter("id"));
        System.out.print(vo.getVal_id());
        if (userRepository.findById(vo.getVal_id()) != null) {
            System.out.println("중복아이디 감지");
            return "<script> alert('중복된 아이디 입니다.');  location.href= '/signin.html'; </script>";
        }
        String password = req.getParameter("password");
        String encoded_password = passwordEncoder.encode(password);
        // vo.setVal_password(password); //not_crypto
        vo.setVal_password(encoded_password);
        vo.setVal_name(req.getParameter("name"));
        vo.setVal_phonenumber(req.getParameter("phone"));
        System.out.print(vo.getVal_password());
        if (vo.getVal_id().equals("") || vo.getVal_name().equals("") || password.equals("")
                || vo.getVal_phonenumber().equals(""))
            return "<script> alert('정보를 모두 입력해주세요.');  location.href= '/signin.html'; </script>";

        userService.joinUser(vo);
        return "<script> alert('가입 되었습니다.'); location.href= '/index.html'; </script>";
    }

    @RequestMapping(value = "/join")
    public String join() {
        return "join";
    }

    @RequestMapping(value = "/failed")
    public String failed() {
        System.out.print("nooo..");
        return "failed";
    }

//    @ResponseBody // return to body
//    @RequestMapping(value = "/idCheck.do", method = RequestMethod.GET)
//    public String idCheck(HttpServletRequest req, CustomerVO vo) {
//        vo.setVal_id(req.getParameter("user_id"));
//        System.out.print(vo.getVal_id());
//        if (userRepository.findById(vo.getVal_id()) != null) {
//            System.out.println("중복아이디 감지");
//            return "<script> alert('중복된 아이디 입니다.');  location.href= '/signIn.html'; </script>";
//        }
//        else
//            return "<script> location.href= '/signIn.html'; </script>";
//    }

    // login

    //로그인 : 로그인시 세션부여
    @ResponseBody // return to body
    @PostMapping(value = "/signIn.do", produces = "text/html; charset=UTF-8")
    public String signIn(HttpSession session, HttpServletRequest req) {
        String id = req.getParameter("id");
        String pw = req.getParameter("password");
        if(id.equals("")) {
            return "<script> alert('아이디를 입력해주세요.');  location.href= '/index.html' ; </script>";
        }
        if(pw.equals("")) {
            return "<script> alert('비밀번호를 입력하세요');  location.href= '/index.html'; </script>";
        }
        if (userRepository.findById(id) == null) {
            return "<script> alert('없는 아이디 입니다.');  location.href= '/index.html'; </script>";
        }

        if (userService.loginCheck(id, pw)) {
            System.out.println("\n" + id + "님 login");
            // 유저의 oid도 세션에 함께 저장한다. (DB연동관련)
            // 세션은 oid, logincheck, id가 저장된다.
            CustomerVO vo = userRepository.findById(id);
            int oid = vo.getVal_oid();
            String name = vo.getVal_name();
            session.setAttribute("oid", oid);
            session.setAttribute("loginCheck", true);
            session.setAttribute("id", id);
            session.setAttribute("name", name);
            return "<script> alert('"+session.getAttribute("name")+"님 로그인 되셨습니다!'); location.href= '/home.html'; </script>";
        } else {
            System.out.println("False");
            return "<script> alert('아이디와 비밀번호가 일치하지 않습니다.');  location.href= '/index.html'; </script>";
        }

    }

    // logout
    @ResponseBody
    @RequestMapping(value = "/logOut.do")
    public String logOut(HttpSession session) {
        session.setAttribute("oid", null);
        session.setAttribute("loginCheck", null);
        session.setAttribute("id", null);
        session.setAttribute("name",null);
        return "<script> alert('로그아웃 되었습니다..');location.href='/index.html'; </script>";
    }

    // page mapping
    // home에는 리다이렉션이 잘이루어짐, .html에는 x
    @ResponseBody
    @RequestMapping(value = "/home")
    public String home(HttpSession session, Model model) {
        if (session.getAttribute("loginCheck") == null)
            return "<script> alert('로그인 후 이용해주시길 바랍니다.');location.href='/index.html'; </script>";   //로그인이 x, 인덱스로 돌려보냄
        //model에 userid속성을 추가하고 값은 id로 설정, 일단 보류
        //model.addAttribute("userid", session.getAttribute("id"));
        return "/home";
    }

    /* 관리자 일단 보류
    @RequestMapping(value = "/adminhome")
    public String adminhome(HttpSession session, Model model) {
        if (session.getAttribute("loginCheck") == null || (int) session.getAttribute("level") == 0)
            return "index";

        model.addAttribute("userid", session.getAttribute("id"));
        return "adminhome";
    }*/

    @RequestMapping(value = "/index")
    public String index(HttpSession session) {
        session.setAttribute("oid", null);
        session.setAttribute("loginCheck", null);
        session.setAttribute("id", null);
        session.setAttribute("name",null);
        return "/index";
    }

    @RequestMapping(value = "/noEventReservation")
    public String noEventReservation() {
        return "noEventReservation";
    }

//    @RequestMapping(value = "/showUserReservation")
//    public String showUserReservation(HttpServletRequest request, Model model) { // 예약리스트 조회관련 코드 추가함 ㅁㅁ
//        HttpSession session = request.getSession(true);// 현재 세션 로드
//        int currentOid = (int) session.getAttribute("oid");
//        String currentid = (String) session.getAttribute("id");
//        List<ReservationVO> list = ReservationService.getReservationListForUser(currentOid);
//        ArrayList<modefiedReservation> list2 = new ArrayList<modefiedReservation>();
//        for (ReservationVO vo : list) {
//            int oid = vo.getVal_oid();
//            int people_number = vo.getVal_people_number();
//            int rank = vo.getVal_rank();
//            int tid = vo.getVal_tid();
//            String start_time = vo.getVal_start_time();
//
//            /// 날짜 형식 변환 코드부분 //////////////////////
//            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd HH시");
//
//            Date date = null;
//            try {
//                date = in.parse(start_time);
//            } catch (ParseException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            String result = out.format(date);
//            /// 날짜 형식 변환 끝 //////////////////////////
//
//            modefiedReservation mReserv = new modefiedReservation();
//            mReserv.setVal_oid(oid);
//            mReserv.setVal_people_number(people_number);
//            mReserv.setVal_rank(rank);
//            // mReserv.setVal_start_time(start_time);
//            mReserv.setVal_start_time(result);
//            mReserv.setVal_tid(tid);
//            list2.add(mReserv);
//        }
//        model.addAttribute("list", list2);
//        model.addAttribute("userid", currentid);
//        return "showUserReservation";
//    }

    @Autowired
    com.module.gomodules.service.ReservationService ReservationService;

    //@Autowired
    //com.module.gomodules.service.TableService TableService;

    @RequestMapping(value = "/addReservation")
    public String addReservation(HttpServletRequest request, ReservationVO vo, Model model) {
        HttpSession session = request.getSession(true);// 현재 세션 로드
        if (session.getAttribute("id") == null) //세션아이디가 존재하지않는다면 index페이지로 보냄
            return "/index";
        vo.setVal_uid((int) session.getAttribute("oid"));// 세션의 oid값 가져오기

        vo.setVal_covers(Integer.parseInt(request.getParameter("num_people")));// 인원수 가져오기

        String date = request.getParameter("date");// 날짜 가져오기
        String time = request.getParameter("time");// 시간 가져오기
        String datetime = date + " " + time;
        vo.setVal_start_time(datetime);// 날짜 + 시간 가져오기
        //vo.setVal_table_number(table_number);

        ReservationService.addReservation(vo);

        //model.addAttribute("userid", session.getAttribute("id"));
        //model.addAttribute("level", session.getAttribute("level"));
        return "/listReservation";
        /*
        // 자동배정의 tid 구하기
        int table_number = 1;
        int mintablewait = 9999;
        int numOftable = TableService.numberofTable(); // 테이블의개수
        for (int j = 1; j <= numOftable; j++) {
            int i = ReservationService.findWaitRank(datetime, j);
            if (i < mintablewait) {
                mintablewait = i;
                table_number = j;
            }
        }

        int i = ReservationService.findWaitRank(datetime, table_number);// 동일날짜 동시간대에 있는 예약의 개수 리턴
        if (i != 0){//이미 해당시간에 예약이 존재한다면
        vo.setVal_wait(1);// 예약이 존재한다.
            vo.setVal_rank(i);// 대기순서는 i
        } else{
            vo.setVal_wait(0);
            vo.setVal_rank(0);
        }
*/
    }
//
//    @Autowired
//    EventService EventService;
//
//    @RequestMapping(value = "/addReservationEvent")
//    public String addReservationEvent(HttpServletRequest request, ReservationVO vo, EventVO evo, Model model) {
//
//        HttpSession session = request.getSession(true);// 현재 세션 로드
//        if (session.getAttribute("id") == null)
//            return "/index";
//        vo.setVal_uid((int) session.getAttribute("oid"));// 세션의 oid값 가져오기
//        vo.setVal_people_number(Integer.parseInt(request.getParameter("num_people")));// 인원수 가져오기
//
//        String date = request.getParameter("date");// 날짜 가져오기
//        String time = request.getParameter("time");// 시간 가져오기
//        String datetime = date + " " + time;
//        vo.setVal_start_time(datetime);// 날짜 + 시간 가져오기
//
//        // 자동배정의 tid 구하기
//        int tid = 1;
//        int mintablewait = 9999;
//        int numOftable = TableService.numberofTable(); // 테이블의개수
//        for (int j = 1; j <= numOftable; j++) {
//            int i = ReservationService.findWaitRank(datetime, j);
//            if (i < mintablewait) {
//                mintablewait = i;
//                tid = j;
//            }
//        }
//
//        int i = ReservationService.findWaitRank(datetime, tid);// 동일날짜 동시간대에 있는 예약의 개수 리턴
//        if (i != 0)/* 이미 해당시간에 예약이 존재한다면 */ {
//            vo.setVal_wait(1);// 예약이 존재한다.
//            vo.setVal_rank(i);// 대기순서는 i
//        } else/* 해당시간에 예약이 없다면 */ {
//            vo.setVal_wait(0);
//            vo.setVal_rank(0);
//        }
//
//        vo.setVal_tid(tid);
//        ReservationService.addReservation(vo);
//        // 여기까지 reservation 처리
//        // 여기부터 event 처리
//        int reserv_oid = vo.getVal_oid(); // 새로 추가된 예약의 oid정보 event의 외래 키로 쓰임
//        evo.setVal_rid(reserv_oid);
//        evo.setVal_event_type(request.getParameter("type"));
//        evo.setVal_event_song(request.getParameter("song"));
//        evo.setVal_event_memo(request.getParameter("memo"));
//
//        EventService.addEvent(evo);
//        // 이벤트 저장 끝
//        model.addAttribute("userid", session.getAttribute("id"));
//        model.addAttribute("level", session.getAttribute("level"));
//        return "home";
//    }

}

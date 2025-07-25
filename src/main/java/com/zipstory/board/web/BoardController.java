package com.zipstory.board.web;

import com.zipstory.board.model.*;
import com.zipstory.board.service.BoardService;
import com.zipstory.board.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ServletComponentScan
@RestController
@RequestMapping("/board")
public class BoardController {
	
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	//시큐리티 pw암호화
	@Autowired
	BCryptPasswordEncoder pwdEncode;
	//시큐리티 bean등록
	@Bean
	public BCryptPasswordEncoder getPwdEncode() {
		return new BCryptPasswordEncoder();
	}

	
	@Autowired
	private BoardService boardService;
	
	
	//로그인
	@PostMapping("/checkLogin")
	public Map<String, Object> selectEmpList(UserVO userVo,HttpSession session, HttpServletRequest request, Model model) {
		
		UserVO dbUser = boardService.selectUser(userVo.getUser_id());
		boolean pwdMatch = false;
		if(dbUser != null) {
			pwdMatch = pwdEncode.matches(userVo.getPass(), dbUser.getPass());
		}
		logger.debug("================================");
		logger.debug("로그인 컨트롤러 접속");
		logger.debug("userVo : {}", userVo );
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		int check = 0;
		if(dbUser != null && pwdMatch == true) {
			
			check = 1;
			session.setAttribute("S_USER", dbUser);
			map.put("userVo", dbUser);
		}
		map.put("check", check);
		return map;
	}
	
	//로그아웃
	@PostMapping("/logout")
	public Map<String, Object> logout(HttpServletRequest request, HttpSession session){
		Map<String, Object> map = new HashMap<String, Object>();
		request.getSession().invalidate();
		
		Object obj = request.getSession().getAttribute("S_USER");
		
		if(obj == null) {
			map.put("logoutCheck", 1);
		}else {
			map.put("logoutCheck", 0);
		}
		
		return map;
		
	}
	
	
	
	//게시판 생성
	@PostMapping(path = "insertBoard")
	public Map<String, Object> insertBoard(BoardVO boardVo, Model model) {
		
		logger.debug("================================");
		logger.debug("게시판 생성 컨트롤러 접속");
		logger.debug("boardVo : {}", boardVo );
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		int insertCnt = boardService.insertBoard(boardVo);
		map.put("insertCnt", insertCnt);
		
		return map;
	}
	
	//사용자 전체목록조회
	@PostMapping(path = "selectUserList")
	public Map<String, Object> selectUserList(Model model) {
		
		logger.debug("================================");
		logger.debug("게시판 전체목록조회 컨트롤러 접속");
		Map<String, Object> map = new HashMap<String, Object>();
		List<UserVO> userList = boardService.selectUserList();
		logger.debug("userList : {}", userList);
		logger.debug("================================");
		map.put("userList", userList);
		
		return map;
	}
	
	//게시글 등록
	@PostMapping(path = "insertPost")
	public synchronized Map<String, Object> insertPost(PostVO postVo,HttpSession session, MultipartHttpServletRequest files,  HttpServletRequest request) {
	
		List<MultipartFile> fileList = files.getFiles("uploadFile");
		
		// 세션에 저장된 로그인한 사용자정보를 받아와서 게시글작성자의 아이디로 등록 
		UserVO userVo = (UserVO)(request.getSession().getAttribute("S_USER"));
		postVo.setUser_id(userVo.getUser_id());
		logger.debug("================================");
		logger.debug("게시글 등록 컨트롤러 접속");
		logger.debug("postVo : {}", postVo);
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		int insertCnt = boardService.insertPost(postVo);
		map.put("insertCnt", insertCnt);
		
		
		//파일등록
		String filename = "";
		FileVO attFileVo = new FileVO();
		int max_post_no = boardService.maxPostno();
		map.put("max_post_no", max_post_no);
		attFileVo.setPost_no(max_post_no);
		if(insertCnt == 1) {
			if(fileList!=null) {
				for(int i = 0; i<fileList.size(); i++) {
					MultipartFile uploadFile = fileList.get(i);
					if(uploadFile!=null) {
						if(!("".equals(uploadFile.getOriginalFilename()))) {
							try {
								String uploadPath = "d:" + File.separator + "uploadFile/";
								
								File uploadDir = new File(uploadPath);
								
								if(!uploadDir.exists()) {
									uploadDir.mkdirs();
								}
								String fileExtension = FileUtil.getFileExtension(uploadFile.getOriginalFilename());
								String realfilename = uploadPath + UUID.randomUUID().toString()+fileExtension;
								filename = uploadFile.getOriginalFilename();
								
								uploadFile.transferTo(new File(realfilename));
								
								attFileVo.setFile_nm(filename);
								attFileVo.setFile_route(realfilename);
								
								
								boardService.insertFile(attFileVo);
								
							} catch (IllegalStateException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					
					}
				}
			
			}
		}
		map.put("success", true);
		
		
		
		
		
		return map;
	}
	
	
	//게시글 전체조회
	@PostMapping(path = "selectPostList")
	public Map<String, Object> selectPostList(PageVo pageVo,String pageSizeStr, String searchCheckStr, String searchStr) {

		
		if("".equals(pageSizeStr)) {
			pageVo.setPageSize(10);
		}else {
			pageVo.setPageSize(Integer.parseInt(pageSizeStr));
		}
		
		if("".equals(searchCheckStr)) {
			pageVo.setSearchCheck(3);
		}else {
			pageVo.setSearchCheck(Integer.parseInt(searchCheckStr));
		}

		logger.debug("================================");
		logger.debug("게시글 조회 컨트롤러 접속");
		logger.debug("pageVo : {}", pageVo);
		logger.debug("postList : {}", boardService.selectPostList(pageVo));
		logger.debug("================================");
		
		
		return boardService.selectPostList(pageVo);
	}
	
	
	//게시글 상세조회 postView
	@PostMapping(path = "postView")
	public Map<String, Object> postView(int post_no,HttpServletRequest request,HttpSession session, Model model) {

		logger.debug("================================");
		logger.debug("게시글 상세조회 컨트롤러 접속");
		logger.debug("post_no : {}", post_no);
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		PostVO postVo = boardService.postView(post_no);
		map.put("post", postVo);
		List<CommentsVO> commentsList = boardService.selectCommentsList(post_no);
		map.put("commentsList", commentsList);
		String user_id = postVo.getUser_id();
		String s_user_id =  ((UserVO)request.getSession().getAttribute("S_USER")).getUser_id();
		int admin_code =  ((UserVO)request.getSession().getAttribute("S_USER")).getAdmin_code();
		map.put("s_user_id", s_user_id);
		
		if(user_id.equals(s_user_id)||admin_code==1) {
			map.put("writerCheck", 1);
		}else {
			map.put("writerCheck", 0);
		}
		List<FileVO> fileList = boardService.selectFileList(post_no);
		map.put("fileList", fileList);
		

		return map;
	}
	
	//답글 등록
	@PostMapping(path = "insertReply")
	public synchronized Map<String, Object> insertReply(PostVO postVo,HttpSession session, MultipartHttpServletRequest files, HttpServletRequest request) {
		List<MultipartFile> fileList = files.getFiles("uploadFile");
		
		UserVO userVo = (UserVO)(request.getSession().getAttribute("S_USER"));
		postVo.setUser_id(userVo.getUser_id());
		logger.debug("================================");
		logger.debug("게시글 등록 컨트롤러 접속");
		logger.debug("postVo : {}", postVo);
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		int insertCnt = boardService.insertReply(postVo);
		map.put("insertCnt", insertCnt);
		int max_post_no = boardService.maxPostno();
		map.put("max_post_no", max_post_no);
		String filename = "";
		FileVO attFileVo = new FileVO();
		attFileVo.setPost_no(max_post_no);
		if(insertCnt == 1) {
			if(fileList!=null) {
				for(int i = 0; i<fileList.size(); i++) {
					MultipartFile uploadFile = fileList.get(i);
					if(uploadFile!=null) {
						if(!("".equals(uploadFile.getOriginalFilename()))) {
							try {
								String uploadPath = "d:" + File.separator + "uploadFile/";
								
								File uploadDir = new File(uploadPath);
								
								if(!uploadDir.exists()) {
									uploadDir.mkdirs();
								}
								String fileExtension = FileUtil.getFileExtension(uploadFile.getOriginalFilename());
								String realfilename = uploadPath + UUID.randomUUID().toString()+fileExtension;
								filename = uploadFile.getOriginalFilename();
								
								uploadFile.transferTo(new File(realfilename));
								
								attFileVo.setFile_nm(filename);
								attFileVo.setFile_route(realfilename);
								
								
								boardService.insertFile(attFileVo);
								
							} catch (IllegalStateException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					
					}
				}
			
			}
		}
		map.put("success", true);
		return map;
	}
	
	//유저 등록
	@PostMapping(path = "registUser")
	public Map<String, Object> registUser(UserVO userVo, Model model) {
		
		logger.debug("================================");
		logger.debug("유저 등록 컨트롤러 접속");
		String inputPass = userVo.getPass();
		String pwd = pwdEncode.encode(inputPass);
		userVo.setPass(pwd);
		logger.debug("userVo : {}", userVo);
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		int insertCnt = boardService.registUser(userVo);
		map.put("insertCnt", insertCnt);
		
		return map;
	}
	
	//아이디 중복검사
	@PostMapping(path = "checkUserId")
	public Map<String, Object> checkUserId(String user_id, Model model) {
		
		logger.debug("================================");
		logger.debug("아이디 중복검사 컨트롤러 접속");
		logger.debug("user_id : {}", user_id);
		logger.debug("================================");
		Map<String, Object> map = new HashMap<String, Object>();
		int check = boardService.checkUserId(user_id);
		map.put("check", check);		
		return map;
	}
	
	//게시글 삭제
	@PostMapping(path = "deletePost")
	public Map<String, Object> deletePost(int post_no){
		Map<String, Object> map = new HashMap<String, Object>();
		logger.debug("================================");
		logger.debug("게시글 삭제 컨트롤러 접속");
		logger.debug("post_no : {}", post_no);
		logger.debug("================================");
		int deleteCheck = boardService.deletePost(post_no);
		map.put("deleteCheck", deleteCheck);
		
		
		return map;
	}
	
	
	// 게시글 수정폼 불러오기
	@GetMapping(path = "updatePost")
	public Map<String,Object> updatePost(int post_no){
		Map<String,Object> map = new HashMap<String, Object>();
		
		logger.debug("================================");
		logger.debug("게시글 수정(GET) 컨트롤러 접속");
		logger.debug("post_no : {}", post_no);
		logger.debug("================================");
		map.put("post", boardService.postView(post_no));
		List<FileVO> fileList = boardService.selectFileList(post_no);
		map.put("fileList", fileList);
		
		return map;
	}
	
	// 게시글 수정
	@PostMapping(path = "updatePost")
	public Map<String,Object> updatePost(PostVO postVo,HttpSession session, MultipartHttpServletRequest files,  HttpServletRequest request){
		Map<String,Object> map = new HashMap<String, Object>();
		List<MultipartFile> fileList = files.getFiles("uploadFile");
		logger.debug("================================");
		logger.debug("게시글 수정(POST) 컨트롤러 접속");
		logger.debug("postVo : {}", postVo);
		logger.debug("================================");
		int updateCnt = boardService.updatePost(postVo);
		map.put("updateCnt", updateCnt);
		
		
		//파일등록
		String filename = "";
		FileVO attFileVo = new FileVO();
		int post_no = postVo.getPost_no();
		map.put("post_no", post_no);
		attFileVo.setPost_no(post_no);
		if(updateCnt == 1) {
			if(fileList!=null) {
				for(int i = 0; i<fileList.size(); i++) {
					MultipartFile uploadFile = fileList.get(i);
					if(uploadFile!=null) {
						if(!("".equals(uploadFile.getOriginalFilename()))) {
							try {
								String uploadPath = "d:" + File.separator + "uploadFile/";
								
								File uploadDir = new File(uploadPath);
								
								if(!uploadDir.exists()) {
									uploadDir.mkdirs();
								}
								String fileExtension = FileUtil.getFileExtension(uploadFile.getOriginalFilename());
								String realfilename = uploadPath + UUID.randomUUID().toString()+fileExtension;
								filename = uploadFile.getOriginalFilename();
								
								uploadFile.transferTo(new File(realfilename));
								
								attFileVo.setFile_nm(filename);
								attFileVo.setFile_route(realfilename);
								
								
								boardService.insertFile(attFileVo);
								
							} catch (IllegalStateException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					
					}
				}
			
			}
		}
		
		map.put("success", true);
		return map;
	}
	
	// 댓글 등록
	@PostMapping(path = "insertComment")
	public Map<String,Object> insertComment(CommentsVO commentsVO,HttpSession session, HttpServletRequest request){
		Map<String,Object> map = new HashMap<String, Object>();
		commentsVO.setUser_id(((UserVO)request.getSession().getAttribute("S_USER")).getUser_id());
		logger.debug("================================");
		logger.debug("댓글등록 컨트롤러 접속");
		logger.debug("commentsVO : {}", commentsVO);
		logger.debug("================================");
		map.put("insertCnt", boardService.insertComment(commentsVO));
		
		
		return map;
	}
	
	//댓글 삭제
	@PostMapping(path = "deleteComments")
	public Map<String,Object> deleteComment(int com_no){
		Map<String,Object> map = new HashMap<String, Object>();
		
		logger.debug("================================");
		logger.debug("댓글삭제 컨트롤러 접속");
		logger.debug("com_no : {}", com_no);
		logger.debug("================================");
		map.put("deleteCnt", boardService.deleteComments(com_no));
		
		
		return map;
	}
	

	//게시판 조회 관리자용
	@GetMapping("boardListView")
	public Map<String, Object> boardListView() {
		Map<String, Object> map = new HashMap<String, Object>();
		logger.debug("================================");
		logger.debug("게시판 전체목록조회 컨트롤러 접속");
		logger.debug("boardList : {}", boardService.boardListView());
		logger.debug("================================");
		map.put("boardList", boardService.boardListView());

		return map;
	}
	
	//게시판 활성, 비활성
	@PostMapping("updateBoard")
	public Map<String, Object> updateBoard(BoardVO boardVo){
		Map<String, Object> map = new HashMap<String, Object>();
		logger.debug("================================");
		logger.debug("게시판 활성, 비활성 컨트롤러 접속");
		logger.debug("boardVo : {}", boardVo);
		logger.debug("================================");
		map.put("updateCnt", boardService.updateBoard(boardVo));
		
		
		return map;
	}
	
	//파일 삭제
	@PostMapping("deleteFile")
	public Map<String, Object> deleteFile(int file_no) {
		Map<String, Object> map = new HashMap<String, Object>();
	
		logger.debug("================================");
		logger.debug("파일삭제 컨트롤러 접속");
		logger.debug("file_no : {}", file_no);
		logger.debug("================================");		
		int deleteCnt = boardService.deleteFile(file_no);
		
		map.put("deleteCnt", deleteCnt);
		
		return map;
	}
	
	//관리자 체크
	@PostMapping("adminCheck")
	public Map<String, Object> adminCheck(HttpServletRequest req){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("admin_code",((UserVO)req.getSession().getAttribute("S_USER")).getAdmin_code());
		
		
		
		return map;
	}
	
	
	
	
}
